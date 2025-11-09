package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RejectContractRequest {
    @NotNull(message = "ID booking request không được để trống")
    private java.util.UUID bookingRequestId;

    private String reason;
}
