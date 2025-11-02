package com.web.bookingKol.domain.payment.rest;

import com.web.bookingKol.domain.payment.dtos.SePayWebhookRequest;
import com.web.bookingKol.domain.payment.services.PaymentService;
import com.web.bookingKol.domain.payment.services.SePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class SePayRestController {
    @Autowired
    private SePayService sePayService;
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/sepay/callback")
    public ResponseEntity<?> handleWebhook(@RequestBody SePayWebhookRequest request,
                                           @RequestHeader(name = "Authorization") String authorizationHeader) {
        return ResponseEntity.ok().body(sePayService.handleWebhook(authorizationHeader, request));
    }

    @GetMapping("/check/{contractId}")
    public ResponseEntity<?> checkContractPaymentSuccess(@PathVariable UUID contractId) {
        return ResponseEntity.ok().body(paymentService.checkContractPaymentSuccess(contractId));
    }

    @GetMapping("/check/course/{purchaseId}")
    public ResponseEntity<?> checkCoursePaymentSuccess(@PathVariable UUID purchaseId) {
        return ResponseEntity.ok().body(paymentService.checkPurchasedCoursePackagePaymentSuccess(purchaseId));
    }
}