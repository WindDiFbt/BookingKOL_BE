package com.web.bookingKol.temp_models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "briefs")
public class Brief {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "key_messages", length = Integer.MAX_VALUE)
    private String keyMessages;

    @Column(name = "dos", length = Integer.MAX_VALUE)
    private String dos;

    @Column(name = "donts", length = Integer.MAX_VALUE)
    private String donts;

    @Column(name = "brand_assets_url", length = Integer.MAX_VALUE)
    private String brandAssetsUrl;

    @Column(name = "deliverable_notes", length = Integer.MAX_VALUE)
    private String deliverableNotes;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}