package com.web.bookingKol.domain.payment.dtos.refund;

import com.web.bookingKol.domain.booking.dtos.ContractDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class RefundDTO {
    private UUID id;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Instant refundedAt;
    private Instant createdAt;
    private ContractDTO contract;
    private String bankNumber;
    private String bankName;
}
