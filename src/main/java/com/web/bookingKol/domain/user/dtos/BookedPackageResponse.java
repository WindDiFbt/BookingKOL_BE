package com.web.bookingKol.domain.user.dtos;

import com.web.bookingKol.domain.booking.models.BookingRequest;
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
    private UUID campaignId;
    private String campaignName;
    private String campaignStatus;
    private String objective;
    private BigDecimal targetPrice;
    private Instant startDate;
    private Instant endDate;
    private String recurrencePattern;
    private String campaignNumber;

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

    private UUID bookingRequestId;
    private UUID contractId;



}

