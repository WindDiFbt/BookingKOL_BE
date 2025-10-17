package com.web.bookingKol.domain.kol.dtos.feedbacks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateFeedbackDTO {
    @Min(value = 0, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer professionalismRating;

    @Min(value = 0, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer communicationRating;

    @Min(value = 0, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer timelineRating;

    @Min(value = 0, message = "Detail rating must be at least 0")
    @Max(value = 5, message = "Detail rating must be at most 5")
    private Integer contentQualityRating;
    private Boolean wouldRehire;
    private String commentPublic;
    private String commentPrivate;

}
