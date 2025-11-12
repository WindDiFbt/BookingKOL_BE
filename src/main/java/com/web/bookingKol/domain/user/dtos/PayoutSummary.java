package com.web.bookingKol.domain.user.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PayoutSummary {
    private UUID id;
    private BigDecimal amount;
    private Instant dueDate;
    private String status;
}

