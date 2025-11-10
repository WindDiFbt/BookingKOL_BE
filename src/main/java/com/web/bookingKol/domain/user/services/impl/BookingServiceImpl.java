package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingPackageKol;
import com.web.bookingKol.domain.booking.models.Campaign;
import com.web.bookingKol.domain.file.mappers.FileMapper;
import com.web.bookingKol.domain.file.services.FileService;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.user.dtos.BookKolRequest;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.*;
import com.web.bookingKol.domain.user.services.BookingService;
import com.web.bookingKol.temp_models.PurchasedServicePackage;
import com.web.bookingKol.temp_models.ServicePackage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final ServicePackageRepository servicePackageRepository;
    private final PurchasedServicePackageRepository purchasedServicePackageRepository;
    private final BookingPackageKolRepository bookingPackageKolRepository;
    private final KolProfileRepository kolProfileRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public ApiResponse<?> bookKol(BookKolRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        ServicePackage servicePackage = servicePackageRepository.findById(req.getPackageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ"));

        if (Boolean.FALSE.equals(servicePackage.getAllowKolSelection())) {
            throw new RuntimeException("Gói dịch vụ này hiện không được phép đặt.");
        }

        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID());
        campaign.setCreatedBy(user);
        campaign.setName(req.getCampaignName());
        campaign.setObjective(req.getObjective());
        campaign.setBudgetMin(BigDecimal.ZERO);
        campaign.setBudgetMax(req.getTargetPrice());
        campaign.setStartDate(req.getStartDate());
        campaign.setEndDate(req.getEndDate());
        campaign.setStatus(Enums.BookingStatus.REQUESTED.name());
        campaign.setCreatedAt(Instant.now());
        campaign.setUpdatedAt(Instant.now());
        campaignRepository.save(campaign);

        if (req.getAttachment() != null && !req.getAttachment().isEmpty()) {
            try {
                var fileDTO = fileService.uploadFilePoint(user.getId(), req.getAttachment());
                fileService.createFileUsage(
                        fileMapper.toEntity(fileDTO),
                        campaign.getId(),
                        Enums.TargetType.CAMPAIGN.name(),
                        false
                );

            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi upload file đính kèm: " + e.getMessage(), e);
            }
        }


        PurchasedServicePackage purchased = new PurchasedServicePackage();
        purchased.setId(UUID.randomUUID());
        purchased.setPackageField(servicePackage);
        purchased.setCampaign(campaign);
        purchased.setStatus(Enums.BookingStatus.REQUESTED.name());
        purchased.setStartDate(Instant.now());
        if (req.getEndDate() != null) {
            purchased.setEndDate(req.getEndDate());
        }
        purchased.setRecurrencePattern(req.getRecurrencePattern());
        purchased.setCreatedAt(Instant.now());
        purchased.setUpdatedAt(Instant.now());
        purchasedServicePackageRepository.saveAndFlush(purchased);

        final String packageType = Optional.ofNullable(servicePackage.getPackageType())
                .map(String::trim).orElse("").toLowerCase(Locale.ROOT);

        if ("vip".equals(packageType)) {
            if (req.getKolIds() == null || req.getKolIds().isEmpty()) {
                throw new RuntimeException("Gói VIP yêu cầu chọn ít nhất một KOL");
            }
            for (UUID kolId : req.getKolIds()) {
                KolProfile kol = kolProfileRepository.findById(kolId)
                        .orElseThrow(() -> new RuntimeException("KOL không tồn tại: " + kolId));

                BookingPackageKol record = new BookingPackageKol();
                record.setPurchasedPackage(purchased);
                record.setKol(kol);
                record.setRoleInBooking("KOL");
                record.setCreatedAt(OffsetDateTime.now());
                bookingPackageKolRepository.save(record);
            }

            if (req.getLiveIds() != null && !req.getLiveIds().isEmpty()) {
                for (UUID liveId : req.getLiveIds()) {
                    KolProfile live = kolProfileRepository.findById(liveId)
                            .orElseThrow(() -> new RuntimeException("Trợ LIVE không tồn tại: " + liveId));

                    BookingPackageKol record = new BookingPackageKol();
                    record.setPurchasedPackage(purchased);
                    record.setKol(live);
                    record.setRoleInBooking("LIVE");
                    record.setCreatedAt(OffsetDateTime.now());
                    bookingPackageKolRepository.save(record);
                }
            }
        } else if ("normal".equals(packageType)) {
            System.out.println("Gói NORMAL - chưa gán KOL, chờ admin phân công.");
        } else {
            throw new RuntimeException("Loại gói không hợp lệ: " + servicePackage.getPackageType());
        }

        return ApiResponse.builder()
                .status(200)
                .message(List.of("Tạo chiến dịch và đặt gói thành công"))
                .data(Map.of(
                        "campaignId", campaign.getId(),
                        "campaignName", campaign.getName(),
                        "packageType", servicePackage.getPackageType(),
                        "purchasedPackageId", purchased.getId()
                ))
                .build();
    }

}

