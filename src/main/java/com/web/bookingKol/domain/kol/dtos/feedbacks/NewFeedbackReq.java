package com.web.bookingKol.domain.kol.dtos.feedbacks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewFeedbackReq {

    @NotNull(message = "Professionalism rating is required")
    @Min(value = 1, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer professionalismRating;

    @NotNull(message = "communication rating is required")
    @Min(value = 1, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer communicationRating;

    @NotNull(message = "timeline rating is required")
    @Min(value = 1, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer timelineRating;

    @NotNull(message = "contentQuality rating is required")
    @Min(value = 1, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer contentQualityRating;
    private Boolean wouldRehire;
    private String commentPublic;
    private String commentPrivate;
    private Boolean isPublic;
}
