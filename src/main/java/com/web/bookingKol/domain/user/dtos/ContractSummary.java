package com.web.bookingKol.domain.user.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ContractSummary {
    private UUID id;
    private String contractNumber;
    private String status;
    private BigDecimal amount;
    private Instant signedAtBrand;
    private Instant signedAtKol;
    private List<PayoutSummary> payouts;
}

