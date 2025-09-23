package com.web.bookingKol.domain.kol.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "kol_social_accounts")
public class KolSocialAccount {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Size(max = 255)
    @Column(name = "handle")
    private String handle;

    @Column(name = "profile_url", length = Integer.MAX_VALUE)
    private String profileUrl;

    @Column(name = "follower_count")
    private Long followerCount;

    @Column(name = "avg_views")
    private Long avgViews;

    @Column(name = "avg_engagement", precision = 10, scale = 4)
    private BigDecimal avgEngagement;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @ColumnDefault("false")
    @Column(name = "is_verified")
    private Boolean isVerified;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

}