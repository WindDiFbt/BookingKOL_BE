package com.web.bookingKol.domain.payment.dtos.transaction;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionDTO {
    private Integer id;
    private String gateway;
    private Instant transactionDate;
    private String accountNumber;
    private String subAccount;
    private BigDecimal amountIn;
    private BigDecimal amountOut;
    private BigDecimal accumulated;
    private String code;
    private String transactionContent;
    private String referenceNumber;
    private String body;
    private Instant createdAt;

    private UUID paymentId;
}
