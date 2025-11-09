package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class KolAvailabilityUpdateDTO {
    private UUID availabilityId;
    private Instant newStartAt;
    private Instant newEndAt;
    private String note;
}

