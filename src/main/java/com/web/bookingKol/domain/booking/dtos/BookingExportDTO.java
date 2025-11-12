package com.web.bookingKol.domain.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingExportDTO {
    private UUID bookingId;
    private String contractNumber;
    private String contractStatus;
    private BigDecimal contractAmount;
    private String bookingNumber;
    private String requestNumber;
    private String bookingStatus;
    private String kolName;
    private String brandName;
    private String brandEmail;
    private String brandPhone;
    private String campaignName;
    private String bookingType;
    private String platform;
    private Instant startAt;
    private Instant endAt;
    private Instant signedAtBrand;
    private Instant signedAtKol;
}