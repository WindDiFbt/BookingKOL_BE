package com.web.bookingKol.domain.kol.dtos;

import com.web.bookingKol.domain.kol.models.KolLeaveRequest;
import com.web.bookingKol.domain.user.models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class KolLeaveRequestDTO {
    private UUID id;
    private UUID kolId;
    private UUID availabilityId;
    private String reason;
    private String status;
    private Instant createdAt;
    private Instant approvedAt;
    private String adminNote;
    private String kolName;
    private String kolEmail;

    public KolLeaveRequestDTO(KolLeaveRequest entity) {
        this.id = entity.getId();
        this.kolId = entity.getKol().getId();
        this.availabilityId = entity.getAvailability().getId();
        this.reason = entity.getReason();
        this.status = entity.getStatus();
        this.createdAt = entity.getCreatedAt();
        this.approvedAt = entity.getApprovedAt();
        this.adminNote = entity.getAdminNote();

        User user = entity.getKol().getUser();
        if (user != null) {
            this.kolName = user.getFullName();
            this.kolEmail = user.getEmail();
        }
    }
}
