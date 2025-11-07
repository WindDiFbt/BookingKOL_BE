package com.web.bookingKol.domain.kol.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDTO {
    private UUID availabilityId;
    private Instant startAt;
    private Instant endAt;
}

