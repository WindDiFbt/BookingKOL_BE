package com.web.bookingKol.domain.course.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PurchaseCourseReqDTO {
    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid Vietnamese phone number format.")
    private String phone;
    @Email(message = "Invalid email format.")
    @NotBlank(message = "Email cannot be empty.")
    private String email;
}
