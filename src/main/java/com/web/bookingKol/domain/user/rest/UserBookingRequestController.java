package com.web.bookingKol.domain.user.rest;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.UserBookingRequestResponse;
import com.web.bookingKol.domain.user.services.UserBookingRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/bookings")
@RequiredArgsConstructor
public class UserBookingRequestController {

    private final UserBookingRequestService userBookingRequestService;


    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<PagedResponse<UserBookingRequestResponse>>> getUserBookingRequests(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userBookingRequestService.getUserBookingRequests(email, pageable));
    }


    @PreAuthorize("hasAnyAuthority('USER')")
    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<UserBookingRequestResponse>> getBookingRequestDetail(
            @PathVariable java.util.UUID id
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userBookingRequestService.getBookingRequestDetail(id, email));
    }
}

