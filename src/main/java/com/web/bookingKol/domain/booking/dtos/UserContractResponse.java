package com.web.bookingKol.domain.booking.dtos;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserContractResponse {
    private UUID contractId;
    private String contractNumber;
    private String status;
    private String terms;
    private Instant createdAt;
    private Instant updatedAt;

    private UUID bookingRequestId;
    private String bookingDescription;
    private String bookingStatus;

    private List<ContractPaymentScheduleResponse> paymentSchedules;
}

