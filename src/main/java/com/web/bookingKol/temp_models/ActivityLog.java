package com.web.bookingKol.temp_models;

import com.web.bookingKol.domain.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Size(max = 150)
    @Column(name = "action", length = 150)
    private String action;

    @Size(max = 100)
    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "metadata_json", length = Integer.MAX_VALUE)
    private String metadataJson;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}