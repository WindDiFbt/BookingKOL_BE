package com.web.bookingKol.domain.kol.dtos.feedbacks;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class FeedbackDTO {
    private UUID id;
    private UUID kolId;
    private UUID contractId;
    private UUID reviewerUserId;

    private String reviewerUserName;
    private String reviewerUserAvatarUrl;

    private Double overallRating;
    private Integer professionalismRating;
    private Integer communicationRating;
    private Integer timelineRating;
    private Integer contentQualityRating;
    private String commentPublic;
    private String commentPrivate;
    private Boolean wouldRehire;

    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isPublic;

}
