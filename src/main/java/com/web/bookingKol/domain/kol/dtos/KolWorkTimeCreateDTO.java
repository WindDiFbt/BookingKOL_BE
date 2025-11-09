package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class KolWorkTimeCreateDTO {
    private UUID bookingRequestId;
    private UUID availabilityId;
    private UUID kolId;
    private Instant startAt;
    private Instant endAt;
    private String note;
}
