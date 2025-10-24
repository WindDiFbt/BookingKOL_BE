package com.web.bookingKol.domain.user.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String email;
    private String phone;
    private String fullName;
    private String gender;
    private String address;
    private String avatarUrl;
    private String status;
    private Instant lastLoginAt;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
