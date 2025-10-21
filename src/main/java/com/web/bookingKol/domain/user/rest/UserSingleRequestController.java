package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.booking.services.BookingRequestService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/user/booking/single-requests")
public class UserSingleRequestController {
    @Autowired
    private BookingRequestService bookingRequestService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSingleRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) LocalDate startAt,
                                                 @RequestParam(required = false) LocalDate endAt,
                                                 @RequestParam(required = false) LocalDate createdAtFrom,
                                                 @RequestParam(required = false) LocalDate createdAtTo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestUser(userId, status, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getDetailSingleRequestByKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable UUID requestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.getDetailSingleRequestUser(requestId, userId));
    }
}
