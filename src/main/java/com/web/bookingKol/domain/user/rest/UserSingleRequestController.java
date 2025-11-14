package com.web.bookingKol.domain.user.rest;

import com.web.bookingKol.domain.booking.dtos.UpdateBookingReqDTO;
import com.web.bookingKol.domain.booking.services.BookingRequestService;
import com.web.bookingKol.domain.payment.dtos.refund.CancellationRequest;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/booking/single-requests")
public class UserSingleRequestController {
    @Autowired
    private BookingRequestService bookingRequestService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllSingleRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @RequestParam(required = false) String status,
                                                 @RequestParam(required = false) String requestNumber,
                                                 @RequestParam(required = false) LocalDate startAt,
                                                 @RequestParam(required = false) LocalDate endAt,
                                                 @RequestParam(required = false) LocalDate createdAtFrom,
                                                 @RequestParam(required = false) LocalDate createdAtTo,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.getAllSingleRequestUser(userId, status, requestNumber, startAt, endAt, createdAtFrom, createdAtTo, page, size));
    }

    @GetMapping("/detail/{requestId}")
    public ResponseEntity<?> getDetailSingleRequestByKol(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable UUID requestId) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.getDetailSingleRequestUser(requestId, userId));
    }

    @PatchMapping("/update/{requestId}")
    public ResponseEntity<?> updateBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable UUID requestId,
                                                  @RequestPart(value = "attachedFiles", required = false) List<MultipartFile> attachedFiles,
                                                  @RequestPart(required = false) @Valid UpdateBookingReqDTO updateBookingReqDTO,
                                                  @RequestPart(required = false) List<UUID> fileIdsToDelete) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.updateBookingRequest(userId, requestId, updateBookingReqDTO, attachedFiles, fileIdsToDelete));
    }

    @PostMapping("/cancel/{requestId}")
    public ResponseEntity<?> cancelBookingRequest(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @PathVariable UUID requestId,
                                                  @RequestBody @Valid CancellationRequest cancellationRequest) {
        UUID userId = userDetails.getId();
        return ResponseEntity.ok(bookingRequestService.cancelBookingRequest(userId, requestId, cancellationRequest.getBankNumber(), cancellationRequest.getBankName(),
                cancellationRequest.getReason(), cancellationRequest.getOwnerName()));
    }
}
