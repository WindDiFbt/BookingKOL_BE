package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.payment.services.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/refunds")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminRefundRestController {
    @Autowired
    private RefundService refundService;

    @PatchMapping("/confirm/{refundId}")
    public ResponseEntity<?> confirmRefunded(@PathVariable UUID refundId) {
        return ResponseEntity.ok(refundService.confirmRefunded(refundId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllRefunds(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) UUID contractId,
                                           Pageable pageable) {
        return ResponseEntity.ok(refundService.getRefunds(pageable, status, contractId));
    }

    @GetMapping("/detail/{refundId}")
    public ResponseEntity<?> getDetailRefund(@PathVariable UUID refundId) {
        return ResponseEntity.ok(refundService.detailRefund(refundId));
    }
}
