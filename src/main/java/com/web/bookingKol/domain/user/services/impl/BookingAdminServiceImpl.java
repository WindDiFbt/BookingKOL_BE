package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingPackageKol;
import com.web.bookingKol.domain.user.dtos.BookedPackageResponse;
import com.web.bookingKol.domain.user.dtos.KolInfo;
import com.web.bookingKol.domain.user.dtos.UpdateBookingStatusRequest;
import com.web.bookingKol.domain.user.repositories.BookingPackageKolRepository;
import com.web.bookingKol.domain.user.repositories.PurchasedServicePackageRepository;
import com.web.bookingKol.domain.user.services.BookingAdminService;
import com.web.bookingKol.temp_models.PurchasedServicePackage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingAdminServiceImpl implements BookingAdminService {

    private final PurchasedServicePackageRepository purchasedRepo;
    private final BookingPackageKolRepository bookingPackageKolRepository;

    @Override
    public ApiResponse<PagedResponse<BookedPackageResponse>> getAllBookings(
            String search, Instant startDate, Instant endDate,String packageType, Pageable pageable) {

        Specification<PurchasedServicePackage> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isBlank()) {
            String keyword = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("campaign").get("name")), keyword),
                    cb.like(cb.lower(root.get("servicePackage").get("name")), keyword),
                    cb.like(cb.lower(root.get("status")), keyword),
                    cb.like(cb.lower(root.get("bookingRequest").get("bookingNumber")), keyword)
            ));
        }


        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("endDate"), endDate));
        }

        if (packageType != null && !packageType.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("packageField").get("packageType")), packageType.toLowerCase())
            );
        }


        Page<PurchasedServicePackage> page = purchasedRepo.findAll(spec, pageable);

        Page<BookedPackageResponse> response = page.map(p -> {
            List<BookingPackageKol> kolLinks = bookingPackageKolRepository.findByPurchasedPackageId(p.getId());

            List<KolInfo> kols = kolLinks.stream()
                    .filter(b -> "KOL".equalsIgnoreCase(b.getRoleInBooking()))
                    .map(b -> KolInfo.builder()
                            .id(b.getKol().getId())
                            .displayName(b.getKol().getDisplayName())
                            .build())
                    .collect(Collectors.toList());

            List<KolInfo> lives = kolLinks.stream()
                    .filter(b -> "LIVE".equalsIgnoreCase(b.getRoleInBooking()))
                    .map(b -> KolInfo.builder()
                            .id(b.getKol().getId())
                            .displayName(b.getKol().getDisplayName())
                            .build())
                    .collect(Collectors.toList());

            return BookedPackageResponse.builder()
                    .id(p.getId())
                    .bookingNumber(p.getBookingNumber())
                    .campaignName(p.getCampaign() != null ? p.getCampaign().getName() : null)
                    .objective(p.getCampaign() != null ? p.getCampaign().getObjective() : null)
                    .budgetMin(p.getCampaign() != null ? p.getCampaign().getBudgetMin() : null)
                    .budgetMax(p.getCampaign() != null ? p.getCampaign().getBudgetMax() : null)
                    .startDate(p.getCampaign() != null ? p.getCampaign().getStartDate() : null)
                    .endDate(p.getCampaign() != null ? p.getCampaign().getEndDate() : null)
                    .recurrencePattern(p.getRecurrencePattern())
                    .packageName(p.getPackageField() != null ? p.getPackageField().getName() : null)
                    .packageType(p.getPackageField() != null ? p.getPackageField().getPackageType() : null)
                    .price(p.getPrice() != null ? p.getPrice().doubleValue() : null)
                    .status(p.getStatus())
                    .buyerEmail(
                            p.getCampaign() != null && p.getCampaign().getCreatedBy() != null
                                    ? p.getCampaign().getCreatedBy().getEmail()
                                    : null
                    )
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .kols(kols)
                    .lives(lives)
                    .build();
        });

        return ApiResponse.<PagedResponse<BookedPackageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách booking thành công"))
                .data(PagedResponse.fromPage(response))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<?> updateBookingStatus(UUID bookingId, UpdateBookingStatusRequest request) {
        PurchasedServicePackage booking = purchasedRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn booking với ID: " + bookingId));

        String newStatus = request.getStatus().trim().toUpperCase(Locale.ROOT);

        boolean isValid = false;
        for (Enums.BookingStatus s : Enums.BookingStatus.values()) {
            if (s.name().equals(newStatus)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
        }

        booking.setStatus(newStatus);
        booking.setUpdatedAt(Instant.now());
        purchasedRepo.save(booking);
        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Cập nhật trạng thái đơn booking thành công"))
                .data(Map.of(
                        "bookingId", booking.getId(),
                        "newStatus", newStatus,
                        "updatedAt", booking.getUpdatedAt()
                ))
                .build();
    }
}

