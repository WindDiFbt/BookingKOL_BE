package com.web.bookingKol.domain.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KolParticipantResponse {
    private UUID kolId;
    private String kolName;
    private String role;
    private String kolAvatar;
}

