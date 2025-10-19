package com.web.bookingKol.domain.payment.dtos.transaction;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionStatsDTO {
    private long totalTransactions;
    private long completedCount;
    private long failedCount;
    private long orphanedCount;
    private BigDecimal totalAmountIn;
    private Instant startDate;
    private Instant endDate;
}
