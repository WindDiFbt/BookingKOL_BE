package com.web.bookingKol.domain.booking.dtos.livestreamMetric;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class LivestreamMetricDTO {
    private Integer id;
    private UUID kolWorkTimeId;
    private UUID bookingId;
    private String requestNumber;
    private BigDecimal revenue;

    private Integer liveViewsOver1min;
    private Integer viewsUnder1min;
    private Integer commentsIn1min;
    private Integer totalComments;
    private Integer addToCartIn1min;
    private Integer totalViews;

    private Integer avgViewDuration;

    private Integer pcu;
    private BigDecimal productClickRate;
    private BigDecimal orderConversionRate;
    private BigDecimal gpm;
    private Integer totalOrders;
    private Integer buyers;
    private BigDecimal avgOrderValue;
    private Integer productsSold;

    private Boolean isConfirmed;

    private Instant createdAt;
    private Instant confirmedAt;
}
