package com.web.bookingKol.domain.admin;

import com.web.bookingKol.domain.payment.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/superadmin/transactions")
public class SuperAdminTransactionController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllTransactions(@RequestParam(required = false) Instant startAt,
                                                @RequestParam(required = false) Instant endAt,
                                                @RequestParam(required = false) String status,
                                                Pageable pageable) {
        return ResponseEntity.ok().body(transactionService.getAllTransactions(startAt, endAt, status, pageable));
    }

    @GetMapping("/detail/{transactionId}")
    public ResponseEntity<?> getDetailTransaction(@PathVariable Integer transactionId) {
        return ResponseEntity.ok().body(transactionService.getDetailTransaction(transactionId));
    }
}
