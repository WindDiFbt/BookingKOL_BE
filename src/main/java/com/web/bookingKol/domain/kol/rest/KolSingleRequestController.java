package com.web.bookingKol.domain.kol.rest;

import com.web.bookingKol.domain.booking.services.BookingRequestService;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/kol/booking/single-requests")
public class KolSingleRequestController {
    @Autowired
    private BookingRequestService bookingRequestService;
    @Autowired
    private UserRepository userRepository;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestKol(user.getKolProfile().getId(), status, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getDetailSingleRequestByKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable UUID requestId) {
        UUID userId = userDetails.getId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(bookingRequestService.getDetailSingleRequestKol(requestId, user.getKolProfile().getId()));
    }
}
