package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.config.CacheConfig;
import com.web.bookingKol.domain.booking.dtos.BookingSingleReqDTO;
import com.web.bookingKol.domain.booking.dtos.SoftHoldDetails;
import com.web.bookingKol.domain.booking.dtos.SoftHoldSlotDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Service
public class SoftHoldBookingService {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private BookingValidationService bookingValidationService;
    @Autowired
    private KolProfileRepository kolProfileRepository;
    private final Logger logger = Logger.getLogger("SOFT_HOLD_SERVICE");

    public String generateHoldKey(UUID kolId, String startTimeIso, String endTimeIso) {
        return kolId + "_" + startTimeIso + "_" + endTimeIso;
    }

    public ApiResponse<SoftHoldSlotDTO> attemptHoldSlot(UUID kolId, Instant startTime, Instant endTime, String holdingUserId) {
        KolProfile kol = kolProfileRepository.findById(kolId).
                orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Kol"));
        BookingSingleReqDTO bookingSingleReqDTO = new BookingSingleReqDTO();
        bookingSingleReqDTO.setKolId(UUID.randomUUID());
        bookingSingleReqDTO.setStartAt(startTime);
        bookingSingleReqDTO.setEndAt(endTime);
        bookingValidationService.validateBookingRequest(bookingSingleReqDTO, kol);
        Cache cache = cacheManager.getCache(CacheConfig.SOFT_HOLD_CACHE);
        if (cache == null) {
            return ApiResponse.<SoftHoldSlotDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Giữ chỗ tạm thời: Thành công"))
                    .data(SoftHoldSlotDTO.builder().kolId(kolId).startTimeIso(startTime).endTimeIso(endTime).build())
                    .build();
        }
        synchronized (kolId.toString().intern()) {
            Map<String, SoftHoldDetails> softHoldsMap = cache.get(kolId, ConcurrentHashMap::new);
            Map<String, SoftHoldDetails> updatedHoldsMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, SoftHoldDetails> entry : softHoldsMap.entrySet()) {
                SoftHoldDetails existingHold = entry.getValue();
                if (!existingHold.userId().equals(holdingUserId)) {
                    boolean isOverlapping = startTime.isBefore(existingHold.endAt()) && endTime.isAfter(existingHold.startAt());
                    if (isOverlapping) {
                        throw new IllegalArgumentException("Khung giờ trùng với một người dùng khác đang giữ chỗ: THẤT BẠI");
                    }
                    updatedHoldsMap.put(entry.getKey(), existingHold);
                }
            }
            String newSlotId = startTime.toString() + "_" + endTime.toString();
            SoftHoldDetails previousHold = softHoldsMap.get(newSlotId);
            if (previousHold != null && previousHold.userId().equals(holdingUserId)) {
                updatedHoldsMap.put(newSlotId, previousHold);
                cache.put(kolId, updatedHoldsMap);
                return ApiResponse.<SoftHoldSlotDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message(List.of("Bạn đã giữ chỗ này: THÀNH CÔNG (TTL đã được làm mới)"))
                        .data(null)
                        .build();
            }
            SoftHoldDetails newHold = new SoftHoldDetails(holdingUserId, startTime, endTime.plus(Duration.ofHours(1)));
            updatedHoldsMap.put(newSlotId, newHold);
            cache.put(kolId, updatedHoldsMap);
            return ApiResponse.<SoftHoldSlotDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message(List.of("Giữ chỗ THÀNH CÔNG"))
                    .data(SoftHoldSlotDTO.builder().kolId(kolId).startTimeIso(startTime).endTimeIso(endTime).build())
                    .build();
        }
    }

    @Cacheable(value = CacheConfig.SOFT_HOLD_CACHE, key = "#root.target.generateHoldKey(#kolId, #startTimeIso, #endTimeIso)", unless = "#result == null")
    public String getHoldingUser(UUID kolId, String startTimeIso, String endTimeIso) {
        return null;
    }

    public void releaseSlot(UUID kolId, Instant startTime, Instant endTime) {
        Cache cache = cacheManager.getCache(CacheConfig.SOFT_HOLD_CACHE);
        if (cache == null) {
            logger.warning("Cache not found: " + CacheConfig.SOFT_HOLD_CACHE);
            return;
        }
        String targetSlotId = startTime.toString() + "_" + endTime.toString();
        synchronized (kolId.toString().intern()) {
            Map<String, SoftHoldDetails> synchronizedMap = cache.get(kolId, Map.class);
            if (synchronizedMap != null) {
                synchronizedMap.remove(targetSlotId);
                cache.put(kolId, synchronizedMap);
            }
        }
    }

    public boolean checkAndReleaseSlot(UUID kolId, Instant startTime, Instant endTime, String currentUserId) {
        Cache cache = cacheManager.getCache(CacheConfig.SOFT_HOLD_CACHE);
        if (cache == null) return true;
        Map<String, SoftHoldDetails> softHoldsMap = cache.get(kolId, Map.class);
        if (softHoldsMap == null || softHoldsMap.isEmpty()) return true;
        String targetSlotId = startTime.toString() + "_" + endTime.toString();
        SoftHoldDetails existingHold = softHoldsMap.get(targetSlotId);
        if (existingHold == null) {
            return true;
        }
        if (existingHold.userId().equals(currentUserId)) {
            synchronized (kolId.toString().intern()) {
                Map<String, SoftHoldDetails> synchronizedMap = cache.get(kolId, Map.class);
                if (synchronizedMap != null) {
                    synchronizedMap.remove(targetSlotId);
                    cache.put(kolId, synchronizedMap);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void logAllSoftHoldKeys() {
        Cache softHoldCache = cacheManager.getCache(CacheConfig.SOFT_HOLD_CACHE);
        if (softHoldCache != null) {
            CaffeineCache caffeineCache = (CaffeineCache) softHoldCache;
            caffeineCache.getNativeCache().asMap().keySet().forEach(kolIdKey -> {
                Map<String, SoftHoldDetails> softHoldsMap = softHoldCache.get(kolIdKey, Map.class);
                System.out.println("\n--- KOL ID: " + kolIdKey + " ---");
                if (softHoldsMap != null) {
                    softHoldsMap.forEach((slotId, holdDetails) -> {
                        System.out.println("  Slot ID: " + slotId +
                                " | User ID: " + holdDetails.userId() +
                                " | Time: " + holdDetails.startAt() + " -> " + holdDetails.endAt());
                    });
                }
            });
        }
    }
}
