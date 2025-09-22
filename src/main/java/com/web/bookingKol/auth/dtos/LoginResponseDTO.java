package com.web.bookingKol.auth.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String type = "Bearer";
    private String id;
    private List<String> roles;
}
