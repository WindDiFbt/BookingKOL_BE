package com.web.bookingKol.domain.payment.dtos.transaction;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TransactionResult {
    private UUID contractId;
    private String status;
    private Integer transactionId;
}
