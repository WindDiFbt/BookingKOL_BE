package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class KolProfileDTO {
    private UUID id;
    private UUID userId;
    private String displayName;
    private String bio;
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

    private Set<CategoryDTO> categories;
}
