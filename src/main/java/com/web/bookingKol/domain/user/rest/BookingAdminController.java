package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.*;
import com.web.bookingKol.domain.user.services.AdminBookingRequestService;

import com.web.bookingKol.domain.user.services.AdminBookingRequestViewService;
import com.web.bookingKol.domain.user.services.BookingAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingAdminService bookingAdminService;
    private final AdminBookingRequestService bookingRequestService;
    private final AdminBookingRequestService adminBookingRequestService;


    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookedPackageResponse>>> getAllBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String packageType,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(bookingAdminService.getAllBookings(search, startDate, endDate,packageType, pageable));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<?>> updateBookingStatus(
            @PathVariable UUID id,
            @RequestBody UpdateBookingStatusRequest request
    ) {
        return ResponseEntity.ok(bookingAdminService.updateBookingStatus(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createBookingRequest(
            @RequestBody AdminCreateBookingRequestDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bookingRequestService.createBookingRequest(dto, email));
    }


    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<PagedResponse<AdminBookingRequestResponse>>> getAllBookingRequests(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(bookingRequestService.getAllBookingRequests(pageable));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PutMapping("/requests/status/{id}")
    public ResponseEntity<ApiResponse<?>> updateBookingRequestStatus(
            @PathVariable java.util.UUID id,
            @RequestBody com.web.bookingKol.domain.user.dtos.UpdateBookingRequestStatusDTO dto
    ) {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(bookingRequestService.updateBookingRequestStatus(id, dto, email));
    }




    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping("/detail/{bookingRequestId}")
    public ResponseEntity<ApiResponse<AdminBookingRequestResponse>> getBookingRequestDetail(
            @PathVariable UUID bookingRequestId) {
        return ResponseEntity.ok(bookingRequestService.getBookingRequestDetail(bookingRequestId));
    }

    // xem chi tiết campaign
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping("/admin/{campaignId}")
    public ResponseEntity<ApiResponse<CampaignDetailResponse>> getCampaignDetail(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(adminBookingRequestService.getCampaignDetail(campaignId));
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/user/{campaignId}")
    public ResponseEntity<ApiResponse<CampaignDetailResponse>> getCampaignDetailUser(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(adminBookingRequestService.getCampaignDetail(campaignId));
    }






}

