package com.web.bookingKol.domain.kol.models;

import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KolWorkTimeDTO {
    private UUID id;
    private Instant startAt;
    private Instant endAt;
    private String note;
    private String status;

    public KolWorkTimeDTO(KolWorkTime entity) {
        this.id = entity.getId();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.note = entity.getNote();
        this.status = entity.getStatus();
    }
}

