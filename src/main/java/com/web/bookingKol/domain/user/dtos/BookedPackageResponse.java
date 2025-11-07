package com.web.bookingKol.domain.user.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookedPackageResponse {
    private UUID id;
    private String campaignName;
    private String objective;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Instant startDate;
    private Instant endDate;
    private String recurrencePattern;

    private String packageName;
    private String packageType;
    private Double price;
    private String status;
    private String bookingNumber;


    private String buyerEmail;
    private List<KolInfo> kols;
    private List<KolInfo> lives;

    private Instant createdAt;
    private Instant updatedAt;
}

