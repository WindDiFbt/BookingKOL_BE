package com.web.bookingKol.domain.kol.dtos;


import com.web.bookingKol.domain.kol.models.KolCancelRequest;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class KolCancelRequestDTO {
    private UUID id;
    private UUID kolId;
    private UUID workTimeId;
    private String reason;
    private String status;
    private Instant createdAt;
    private Instant approvedAt;
    private String adminNote;

    public KolCancelRequestDTO() {}

    public KolCancelRequestDTO(KolCancelRequest entity) {
        this.id = entity.getId();
        this.kolId = entity.getKol().getId();
        this.workTimeId = entity.getWorkTime().getId();
        this.reason = entity.getReason();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.approvedAt = entity.getApprovedAt();
        this.adminNote = entity.getAdminNote();
    }
}

