package com.web.bookingKol.domain.kol.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class KolSocialAccountDTO {
    private UUID id;
    private UUID kolId;
    private UUID platformId;
    private String handle;
    private String profileUrl;
    private Long followerCount;
    private Long avgViews;
    private BigDecimal avgEngagement;
    private Instant lastSyncedAt;
    private Boolean isVerified;
}
