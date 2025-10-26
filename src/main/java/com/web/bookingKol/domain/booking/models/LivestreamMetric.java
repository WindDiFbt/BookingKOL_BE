package com.web.bookingKol.domain.booking.models;

import com.web.bookingKol.domain.kol.models.KolWorkTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "livestream_metrics")
public class LivestreamMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal revenue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kol_work_time_id")
    private KolWorkTime kolWorkTime;

    @Column(name = "live_views_over_1min")
    private Integer liveViewsOver1min;

    @Column(name = "views_under_1min")
    private Integer viewsUnder1min;

    @Column(name = "comments_in_1min")
    private Integer commentsIn1min;

    @Column(name = "total_comments")
    private Integer totalComments;

    @Column(name = "add_to_cart_in_1min")
    private Integer addToCartIn1min;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "avg_view_duration")
    private Integer avgViewDuration;

    @Column(name = "pcu")
    private Integer pcu;

    @Column(name = "product_click_rate", precision = 5, scale = 2)
    private BigDecimal productClickRate;

    @Column(name = "order_conversion_rate", precision = 5, scale = 2)
    private BigDecimal orderConversionRate;

    @Column(name = "gpm", precision = 18, scale = 2)
    private BigDecimal gpm;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "buyers")
    private Integer buyers;

    @Column(name = "avg_order_value", precision = 18, scale = 2)
    private BigDecimal avgOrderValue;

    @Column(name = "products_sold")
    private Integer productsSold;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;
}