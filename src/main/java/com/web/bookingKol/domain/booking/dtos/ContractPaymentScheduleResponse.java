package com.web.bookingKol.domain.booking.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ContractPaymentScheduleResponse {

    private UUID id;
    private UUID contractId;
    private UUID bookingRequestId;
    private Integer installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private String status;

    private Instant createdAt;
    private Instant updatedAt;
}

