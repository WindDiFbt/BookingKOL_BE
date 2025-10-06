package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.services.KolAvailabilityService;
import com.web.bookingKol.integration.google.GoogleCalendarService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KolAvailabilityServiceImpl implements KolAvailabilityService {

    private final KolAvailabilityRepository kolAvailabilityRepository;
    private final UserRepository userRepository;
    private final GoogleCalendarService googleCalendarService;

    @Override
    public ApiResponse<List<KolAvailabilityDTO>> getKolSchedule(UUID userId, OffsetDateTime start, OffsetDateTime end, String status, String note) {
        var list = kolAvailabilityRepository.search(userId, start, end, status, note)
                .stream()
                .map(KolAvailabilityDTO::new)
                .toList();

        return ApiResponse.<List<KolAvailabilityDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch thành công"))
                .data(list)
                .build();
    }

    @Override
    public ApiResponse<KolAvailabilityDTO> createAvailability(UUID userId, KolAvailabilityDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy KOL"));

        KolAvailability entity = new KolAvailability();
        entity.setId(UUID.randomUUID());
        entity.setUser(user);
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setStatus(dto.getStatus());
        entity.setNote(dto.getNote());

        kolAvailabilityRepository.save(entity);

        try {
            String googleEventId = googleCalendarService.upsertEvent(userId, entity);
            entity.setGoogleEventId(googleEventId);
            kolAvailabilityRepository.save(entity);
        } catch (Exception e) {
            entity.setSyncStatus("FAILED");
            kolAvailabilityRepository.save(entity);
        }

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message(List.of("Tạo lịch thành công"))
                .data(new KolAvailabilityDTO(entity))
                .build();
    }

    @Override
    public ApiResponse<KolAvailabilityDTO> updateAvailability(UUID userId, UUID availabilityId, KolAvailabilityDTO dto) {
        KolAvailability entity = kolAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch"));

        if (!entity.getUser().getId().equals(userId))
            throw new SecurityException("Không có quyền sửa lịch này");

        if (dto.getStartAt() != null) entity.setStartAt(dto.getStartAt());
        if (dto.getEndAt() != null) entity.setEndAt(dto.getEndAt());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getNote() != null) entity.setNote(dto.getNote());

        kolAvailabilityRepository.save(entity);

        try {
            String googleEventId = googleCalendarService.upsertEvent(userId, entity);
            entity.setGoogleEventId(googleEventId);
            entity.setSyncStatus("SUCCESS");
        } catch (Exception e) {
            entity.setSyncStatus("FAILED");
        }
        kolAvailabilityRepository.save(entity);

        return ApiResponse.<KolAvailabilityDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Cập nhật lịch thành công"))
                .data(new KolAvailabilityDTO(entity))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteAvailability(UUID userId, UUID availabilityId) {
        KolAvailability entity = kolAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy lịch"));

        if (!entity.getUser().getId().equals(userId))
            throw new SecurityException("Không có quyền xoá lịch này");

        try {
            if (entity.getGoogleEventId() != null)
                googleCalendarService.deleteEvent(userId, entity.getGoogleEventId(), entity.getGoogleCalendarId());
        } catch (Exception ignored) {}

        kolAvailabilityRepository.delete(entity);

        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xóa lịch thành công"))
                .build();
    }
}
