package com.web.bookingKol.domain.kol.dtos;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import com.web.bookingKol.domain.kol.dtos.feedbacks.FeedbackUserViewDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
public class KolDetailDTO {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String displayName;
    private String avatarUrl;
    private Date dob;
    private String bio;
    private String experience;
    private String country;
    private String city;
    private String languages;
    private String rateCardNote;
    private BigDecimal minBookingPrice;
    private Boolean isAvailable;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private Double overallRating;
    private Integer feedbackCount;
    private Enums.Roles role;

    private Set<CategoryDTO> categories;
    private Set<FileUsageDTO> fileUsageDtos;
    private Set<FeedbackUserViewDTO> feedbacks;
}
