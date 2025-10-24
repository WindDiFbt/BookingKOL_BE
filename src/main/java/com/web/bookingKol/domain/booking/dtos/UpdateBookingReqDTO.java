package com.web.bookingKol.domain.booking.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBookingReqDTO {
    @Size(max = 255, message = "Full name must be less than 255 characters.")
    private String fullName;
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid Vietnamese phone number format.")
    private String phone;
    @NotBlank(message = "Email cannot be empty.")
    private String email;
    private String description;
    private String location;
}
