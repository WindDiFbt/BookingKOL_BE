package com.web.bookingKol.domain.payment.dtos.refund;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancellationRequest {
    @NotBlank(message = "Bank number is required")
    @Size(max = 20, message = "Bank number max 20 characters")
    @Pattern(regexp = "^[0-9]*$", message = "Bank number must only contain digits (0-9)")
    private String bankNumber;

    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name max 255 characters")
    private String bankName;
}
