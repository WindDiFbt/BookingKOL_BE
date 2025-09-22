package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "deliverables")
public class Deliverable {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Size(max = 50)
    @Column(name = "deliverable_type", length = 50)
    private String deliverableType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @Size(max = 255)
    @Column(name = "title")
    private String title;

    @Column(name = "requirements", length = Integer.MAX_VALUE)
    private String requirements;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Size(max = 50)
    @Column(name = "approval_status", length = 50)
    private String approvalStatus;

    @Column(name = "asset_url", length = Integer.MAX_VALUE)
    private String assetUrl;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}