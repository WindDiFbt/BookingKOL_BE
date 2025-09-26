package com.web.bookingKol.domain.kol.models;

import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.temp_models.Contract;
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
@Table(name = "kol_feedbacks")
public class KolFeedback {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kol_id", nullable = false)
    private KolProfile kol;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private User reviewerUser;

    @NotNull
    @Column(name = "overall_rating", nullable = false)
    private Double overallRating;

    @Column(name = "professionalism_rating")
    private Integer professionalismRating;

    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Column(name = "timeline_rating")
    private Integer timelineRating;

    @Column(name = "content_quality_rating")
    private Integer contentQualityRating;

    @Column(name = "would_rehire")
    private Boolean wouldRehire;

    @Column(name = "comment_public", length = Integer.MAX_VALUE)
    private String commentPublic;

    @Column(name = "comment_private", length = Integer.MAX_VALUE)
    private String commentPrivate;

    @ColumnDefault("true")
    @Column(name = "is_public")
    private Boolean isPublic;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}