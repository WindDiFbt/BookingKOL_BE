package com.web.bookingKol.domain.admin.dashboard;

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
    private DashboardStatsDTO stats;
    private List<ChartDataPointDTO> revenueChart;
    private List<ChartDataPointDTO> statusBreakdown;
    private List<UpcomingBookingDTO> UpcomingWorkTimes;
    private List<RecentBookingDTO> RecentWorkTimes;
    private List<RecentBookingDTO> completedRecentWorkTimes;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DashboardStatsDTO {
        private BigDecimal totalRevenue;
        private BigDecimal earnedRevenue;
        private BigDecimal pendingRevenue;
        private BigDecimal cancelledLoss;
        private long totalBookings;
        private long completedBookings;
        private long inProgressBookings;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ChartDataPointDTO {
        private String label;
        private Number value;
    }

    @Getter
    @Setter
    public static class UpcomingBookingDTO {
        private UUID bookingId;
        private String clientName;
        private Instant startAt;
        private Instant endAt;
        private String location;
        private BigDecimal amount;
        private String status;
    }

    @Getter
    @Setter
    public static class RecentBookingDTO {
        private String requestNumber;
        private String clientName;
        private Instant startAt;
        private String status;
        private BigDecimal amount;
    }
}
