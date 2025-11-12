package com.web.bookingKol.domain.admin.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.web.bookingKol.domain.admin.dashboard.course.CourseRevenueDTO;
import com.web.bookingKol.domain.admin.dashboard.course.RevenueByDateProjection;
import com.web.bookingKol.domain.admin.dashboard.course.RevenueOverviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class AdminDashboardDTO {
    @JsonProperty(index = 1)
    private DashboardStatsDTO stats;
    @JsonProperty(index = 2)
    private List<ChartDataPointDTO> revenueChart;
    @JsonProperty(index = 3)
    private List<ChartDataPointDTO> statusContractBreakdown;
    @JsonProperty(index = 4)
    private List<BookingDTO> upcomingBookingRequests;
    @JsonProperty(index = 5)
    private List<BookingDTO> recentBookingRequests;
    @JsonProperty(index = 6)
    private List<KolBookingCountDTO> topKolsByBookings;
    @JsonProperty(index = 7)
    private List<KolBookingRevenueDTO> topKolsByRevenue;
    @JsonProperty(index = 8)
    private List<BookingStatusCountDTO> statusDistribution;
    @JsonProperty(index = 9)
    private List<PlatformCountDTO> platformDistribution;
    @JsonProperty(index = 10)
    private List<BookingMonthlyTrendDTO> bookingTrend;
    @JsonProperty(index = 11)
    private RevenueOverviewDTO revenueOverview;
    @JsonProperty(index = 12)
    private List<CourseRevenueDTO> courseRevenue;
    @JsonProperty(index = 13)
    private List<RevenueByDateProjection> revenueByDate;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DashboardStatsDTO {
        private long totalBookings;
        private long completedBookings;
        private long inProgressBookings;
        private BigDecimal totalRevenue;
        private BigDecimal earnedRevenue;
        private BigDecimal pendingRevenue;
        private BigDecimal cancelledLoss;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChartDataPointDTO {
        private String label;
        private Number value;
        private double percentage;
    }

    @Getter
    @Setter
    public static class BookingDTO {
        private UUID bookingId;
        private String requestNumber;
        private String clientName;
        private Instant startAt;
        private Instant endAt;
        private String location;
        private String status;
    }
}
