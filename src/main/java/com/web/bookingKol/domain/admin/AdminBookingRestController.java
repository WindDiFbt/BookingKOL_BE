package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.booking.services.BookingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/admin/booking/single-requests")
public class AdminBookingRestController {
    @Autowired
    private BookingRequestService bookingRequestService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSingleRequest(@RequestParam(required = false) String status,
                                                 @RequestParam(required = false) LocalDate startAt,
                                                 @RequestParam(required = false) LocalDate endAt,
                                                 @RequestParam(required = false) LocalDate createdAtFrom,
                                                 @RequestParam(required = false) LocalDate createdAtTo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                null, null, status, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getDetailSingleRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(bookingRequestService.getDetailSingleRequestAdmin(requestId));
    }

    @GetMapping("/all/by-user/{userId}")
    public ResponseEntity<?> getAllSingleRequestByUser(@PathVariable("userId") UUID userId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) LocalDate startAt,
                                                       @RequestParam(required = false) LocalDate endAt,
                                                       @RequestParam(required = false) LocalDate createdAtFrom,
                                                       @RequestParam(required = false) LocalDate createdAtTo,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                null, userId, status, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/all/by-kol/{kolId}")
    public ResponseEntity<?> getAllSingleRequestByKol(@PathVariable("kolId") UUID kolId,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) LocalDate startAt,
                                                      @RequestParam(required = false) LocalDate endAt,
                                                      @RequestParam(required = false) LocalDate createdAtFrom,
                                                      @RequestParam(required = false) LocalDate createdAtTo,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestAdmin(
                kolId, null, status, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }
}
