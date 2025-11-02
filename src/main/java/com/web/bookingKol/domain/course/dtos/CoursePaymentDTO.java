package com.web.bookingKol.domain.course.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CoursePaymentDTO {
    private UUID id;
    private UUID userId;
    private UUID coursePackageId;

    private Long price;
    private Integer discount;
    private Long currentPrice;
    private String email;
    private String phoneNumber;

    private Instant startDate;
    private Instant endDate;
    private String purchasedCourseNumber;
}
