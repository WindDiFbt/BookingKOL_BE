package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class AdminCreateAvailabilityDTO {
    private UUID kolId;
    private Instant startAt;
    private Instant endAt;
}

