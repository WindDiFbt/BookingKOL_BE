package com.web.bookingKol.domain.booking.rest;

import com.web.bookingKol.domain.booking.dtos.BookingSingleReqDTO;
import com.web.bookingKol.domain.booking.dtos.SoftHoldSlotDTO;
import com.web.bookingKol.domain.booking.services.BookingRequestService;
import com.web.bookingKol.domain.booking.services.SoftHoldBookingService;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/booking")
@PreAuthorize("hasAuthority('USER')")
public class BookingRequestRestController {
    @Autowired
    private BookingRequestService bookingRequestService;
    @Autowired
    private SoftHoldBookingService softHoldBookingService;
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/request/single")
    ResponseEntity<?> newBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestPart(value = "attachedFiles", required = false) List<MultipartFile> attachedFiles,
                                        @RequestPart @Valid BookingSingleReqDTO bookingSingleReqDTO) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(bookingRequestService.createBookingSingleReq(userId, bookingSingleReqDTO, attachedFiles));
    }

    @PostMapping("/request/single/confirm/{bookingRequestId}")
    ResponseEntity<?> confirmBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable UUID bookingRequestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(bookingRequestService.confirmBookingSingleReq(bookingRequestId, userId));
    }

    @PatchMapping("/request/single/cancel/{bookingRequestId}")
    ResponseEntity<?> cancelBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable UUID bookingRequestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(bookingRequestService.cancelBookingSingleReq(bookingRequestId, userId));
    }

    @PostMapping("/hold-slot")
    ResponseEntity<?> softHoldSlot(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   @RequestBody SoftHoldSlotDTO softHoldSlotDTO) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(softHoldBookingService.attemptHoldSlot(softHoldSlotDTO.getKolId(),
                softHoldSlotDTO.getStartTimeIso(),
                softHoldSlotDTO.getEndTimeIso(),
                userId.toString()));
    }

    @GetMapping("/listAll")
    ResponseEntity<?> getAll() {
        softHoldBookingService.logAllSoftHoldKeys();
        return ResponseEntity.ok().body("ok");
    }

    @PatchMapping("/request/single/payment/cancel/{requestId}")
    ResponseEntity<?> cancelPayment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @PathVariable UUID requestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(paymentService.cancelPaymentBookingRequest(userId, requestId));
    }

    @PostMapping("/request/single/payment/continue/{requestId}")
    ResponseEntity<?> continuePayment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @PathVariable UUID requestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok().body(bookingRequestService.continueBookingRequestPayment(requestId, userId));
    }
}
