package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TimeRangeDTO {
    private UUID availabilityId;
    private Instant startRemove;
    private Instant endRemove;
}

