package com.web.bookingKol.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "User Name, password or phone number!")
    private String identifier;

    @NotBlank(message = "Password is required!")
    private String password;
}
