package com.web.bookingKol.domain.user.rest;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.BookKolRequest;
import com.web.bookingKol.domain.user.dtos.CampaignDetailResponse;
import com.web.bookingKol.domain.user.services.AdminBookingRequestService;
import com.web.bookingKol.domain.user.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final AdminBookingRequestService adminBookingRequestService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping(value = "/packages", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<?>> bookKol(@ModelAttribute BookKolRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bookingService.bookKol(request, email));
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/user/{campaignId}")
    public ResponseEntity<ApiResponse<CampaignDetailResponse>> getCampaignDetailUser(@PathVariable UUID campaignId) {
        return ResponseEntity.ok(adminBookingRequestService.getCampaignDetail(campaignId));
    }

}

