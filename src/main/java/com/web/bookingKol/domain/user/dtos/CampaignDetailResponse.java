package com.web.bookingKol.domain.user.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CampaignDetailResponse {
    private UUID id;
    private String name;
    private String objective;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private Instant startDate;
    private Instant endDate;
    private String status;
    private String createdByEmail;

    private List<BookingRequestDetail> bookingRequests;

    private List<KolInfo> allKols;
    private List<KolInfo> allLives;
}

