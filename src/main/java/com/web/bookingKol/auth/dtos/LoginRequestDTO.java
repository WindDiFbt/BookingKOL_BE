package com.web.bookingKol.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "User Name, password or phone number!")
    @Pattern(
            regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}|0\\d{9})$",
            message = "Identifier must be a valid email or 10-digit phone number"
    )
    private String identifier;

    @NotBlank(message = "Password is required!")
//    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
