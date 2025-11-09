package com.web.bookingKol.domain.course.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PurchasedCoursePackageDTO {
    private UUID id;
    private UUID userId;
    private UUID coursePackageId;
    private UUID paymentId;

    private Long currentPrice;
    private String status;
    private Boolean isPaid;
    private String email;

    private Instant startDate;
    private Instant endDate;

    private String purchasedCourseNumber;
}
