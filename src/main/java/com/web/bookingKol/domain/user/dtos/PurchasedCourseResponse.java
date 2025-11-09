package com.web.bookingKol.domain.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedCourseResponse {

    private UUID id;
    private String courseName;
    private Long currentPrice;
    private Boolean isPaid;
    private String status;
    private Instant startDate;
    private Instant endDate;
}

