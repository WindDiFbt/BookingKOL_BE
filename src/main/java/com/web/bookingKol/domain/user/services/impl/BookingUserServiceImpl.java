package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingPackageKol;
import com.web.bookingKol.domain.booking.models.Campaign;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.user.dtos.BookedPackageResponse;
import com.web.bookingKol.domain.user.dtos.KolInfo;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.BookingPackageKolRepository;
import com.web.bookingKol.domain.user.repositories.CampaignRepository;
import com.web.bookingKol.domain.user.repositories.PurchasedServicePackageRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.BookingUserService;
import com.web.bookingKol.temp_models.PurchasedServicePackage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingUserServiceImpl implements BookingUserService {

    private final UserRepository userRepository;
    private final PurchasedServicePackageRepository purchasedRepo;
    private final BookingPackageKolRepository bookingPackageKolRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ContractRepository contractRepository;
    private final CampaignRepository campaignRepository;

    @Override
    public ApiResponse<PagedResponse<BookedPackageResponse>> getUserBookings(
            String userEmail,
            String search,
            Instant startDate,
            Instant endDate,
            String packageType,
            Pageable pageable
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Specification<PurchasedServicePackage> spec = (root, query, cb) ->
                cb.equal(root.get("campaign").get("createdBy"), user);

        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("campaign").get("name")), like),
                    cb.like(cb.lower(root.get("packageField").get("name")), like),
                    cb.like(cb.lower(root.get("status")), like)
            ));
        }

        if (packageType != null && !packageType.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("packageField").get("packageType")),
                            packageType.toLowerCase())
            );
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<PurchasedServicePackage> page = purchasedRepo.findAll(spec, sortedPageable);

        Page<BookedPackageResponse> result = page.map(p -> {
            List<BookingPackageKol> links = bookingPackageKolRepository.findByPurchasedPackageId(p.getId());

            List<KolInfo> kols = links.stream()
                    .filter(l -> "KOL".equalsIgnoreCase(l.getRoleInBooking()))
                    .map(l -> KolInfo.builder()
                            .id(l.getKol().getId())
                            .displayName(l.getKol().getDisplayName())
                            .build())
                    .collect(Collectors.toList());

            List<KolInfo> lives = links.stream()
                    .filter(l -> "LIVE".equalsIgnoreCase(l.getRoleInBooking()))
                    .map(l -> KolInfo.builder()
                            .id(l.getKol().getId())
                            .displayName(l.getKol().getDisplayName())
                            .build())
                    .collect(Collectors.toList());

            return BookedPackageResponse.builder()
                    .id(p.getId())
                    .campaignName(p.getCampaign() != null ? p.getCampaign().getName() : null)
                    .campaignId(p.getCampaign().getId())
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

                    .buyerEmail(p.getCampaign() != null && p.getCampaign().getCreatedBy() != null
                            ? p.getCampaign().getCreatedBy().getEmail()
                            : null)

                    .kols(kols)
                    .lives(lives)
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        });

        return ApiResponse.<PagedResponse<BookedPackageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy lịch sử booking thành công"))
                .data(PagedResponse.fromPage(result))
                .build();
    }




    //user hủy đơn campaign
    @Override
    @Transactional
    public ApiResponse<?> cancelBookingRequest(UUID id, String userEmail) {

        var userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("Không tìm thấy người dùng: " + userEmail))
                    .data(null)
                    .build();
        }
        var user = userOpt.get();

        var bookingOpt = bookingRequestRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("Không tìm thấy booking request với ID: " + id))
                    .data(Map.of("bookingRequestId", id))
                    .build();
        }
        var booking = bookingOpt.get();

        if (booking.getCampaign() == null || booking.getCampaign().getCreatedBy() == null) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Booking request không hợp lệ hoặc không gắn với campaign hợp lệ"))
                    .data(Map.of("bookingRequestId", booking.getId()))
                    .build();
        }

        if (!booking.getCampaign().getCreatedBy().getId().equals(user.getId())) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(List.of("Bạn không có quyền hủy booking request này"))
                    .data(Map.of(
                            "bookingRequestId", booking.getId(),
                            "campaignOwner", booking.getCampaign().getCreatedBy().getEmail(),
                            "currentUser", userEmail
                    ))
                    .build();
        }

        booking.setStatus(Enums.BookingStatus.CANCELLED.name());
        booking.setUpdatedAt(Instant.now());
        bookingRequestRepository.save(booking);

        List<Contract> contracts = contractRepository.findByBookingRequest_Id(booking.getId());
        for (Contract c : contracts) {
            c.setStatus(Enums.BookingStatus.CANCELLED.name());
            c.setUpdatedAt(Instant.now());
            contractRepository.save(c);
        }

        Campaign campaign = booking.getCampaign();
        if (campaign != null) {
            campaign.setStatus(Enums.BookingStatus.CANCELLED.name());
            campaignRepository.save(campaign);
        }

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Hủy booking request thành công. Tất cả trạng thái liên quan đã chuyển sang CANCELLED"))
                .data(new Object() {
                    public final UUID bookingRequestId = booking.getId();
                    public final String bookingStatus = booking.getStatus();
                    public final UUID campaignId = campaign != null ? campaign.getId() : null;
                    public final String campaignStatus = campaign != null ? campaign.getStatus() : null;
                    public final List<UUID> contractIds = contracts.stream().map(Contract::getId).toList();
                    public final String contractStatus = Enums.BookingStatus.CANCELLED.name();
                })
                .build();
    }


}


