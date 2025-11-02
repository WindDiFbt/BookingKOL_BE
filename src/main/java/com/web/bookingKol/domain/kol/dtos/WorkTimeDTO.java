package com.web.bookingKol.domain.kol.dtos;


import com.web.bookingKol.domain.kol.models.KolWorkTime;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkTimeDTO {
    private UUID id;
    private Instant startAt;
    private Instant endAt;
    private String note;
    private String status;
    private UUID bookingRequestId;

    public WorkTimeDTO(KolWorkTime entity) {
        this.id = entity.getId();
        this.startAt = entity.getStartAt();
        this.endAt = entity.getEndAt();
        this.note = entity.getNote();
        this.status = entity.getStatus();
        this.bookingRequestId = entity.getBookingRequest() != null
                ? entity.getBookingRequest().getId()
                : null;
    }
}
