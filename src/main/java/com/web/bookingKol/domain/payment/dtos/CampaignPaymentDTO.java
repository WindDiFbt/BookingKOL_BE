package com.web.bookingKol.domain.payment.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CampaignPaymentDTO {
    private UUID contractId;
    private UUID contractPaymentScheduleId;
    private Integer installmentNumber;
    private BigDecimal amount;
    private String qrUrl;
    private String transferContent;

    private String name;
    private String bank;
    private String accountNumber;
}
