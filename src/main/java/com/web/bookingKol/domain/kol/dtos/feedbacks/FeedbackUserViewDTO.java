package com.web.bookingKol.domain.kol.dtos.feedbacks;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class FeedbackUserViewDTO {
    private UUID id;
    private UUID kolId;
    private String reviewerUserName;
    private String reviewerUserAvatarUrl;
    private Double overallRating;
    private Integer professionalismRating;
    private Integer communicationRating;
    private Integer timelineRating;
    private Integer contentQualityRating;
    private String commentPublic;
    private Instant createdAt;
}
