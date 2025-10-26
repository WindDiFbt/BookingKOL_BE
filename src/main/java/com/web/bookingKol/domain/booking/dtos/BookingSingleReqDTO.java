package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingSingleReqDTO {
    @NotNull
    private UUID kolId;
    @NotBlank(message = "Full name cannot be empty.")
    @Size(max = 255, message = "Full name must be less than 255 characters.")
    private String fullName;
    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid Vietnamese phone number format.")
    private String phone;
    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email cannot be empty.")
    private String email;
    @NotNull
    private Instant startAt;
    @NotNull
    private Instant endAt;
    private String description;
    private String location;
}
