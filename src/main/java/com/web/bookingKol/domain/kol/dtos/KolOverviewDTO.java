package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
public class KolOverviewDTO {
    private UUID id;
    private String displayName;
    private String bio;
    private String country;
    private String city;
    private String languages;
    private String rateCardNote;
    private BigDecimal minBookingPrice;
    private Boolean isAvailable;
    private Double overallRating;
    private Integer feedbackCount;

    private Set<CategoryDTO> categories;
}
