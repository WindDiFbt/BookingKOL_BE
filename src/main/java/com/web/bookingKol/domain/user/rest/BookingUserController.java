package com.web.bookingKol.domain.user.rest;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricReqDTO;
import com.web.bookingKol.domain.booking.services.LivestreamMetricService;
import com.web.bookingKol.domain.user.dtos.BookedPackageResponse;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.BookingUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/user/bookings")
@RequiredArgsConstructor
public class BookingUserController {

    private final BookingUserService bookingUserService;
    private final UserRepository userRepository;

    @Autowired
    private LivestreamMetricService livestreamMetricService;

    @PreAuthorize("hasAuthority('USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookedPackageResponse>>> getUserBookings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String packageType,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                bookingUserService.getUserBookings(
                        userDetails.getUsername(),
                        search,
                        startDate,
                        endDate,
                        packageType,
                        pageable
                )
        );
    }


//    //user hủy đơn campaign
//    @PreAuthorize("hasAuthority('USER')")
//    @PutMapping("/cancel/campaign/{id}")
//    public ResponseEntity<ApiResponse<?>> cancelBookingCampaign(@PathVariable UUID id) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        return ResponseEntity.ok(bookingUserService.cancelBookingCampaign(id, email));
//    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/cancel/bookingrequest/{bookingRequestId}")
    public ResponseEntity<ApiResponse<?>> cancelBookingRequest(
            @PathVariable UUID bookingRequestId
    ) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bookingUserService.cancelBookingRequest(bookingRequestId, userEmail));
    }

}

