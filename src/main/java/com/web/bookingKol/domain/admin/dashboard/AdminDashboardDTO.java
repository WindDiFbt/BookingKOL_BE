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
    private List<ChartDataPointDTO> statusContractBreakdown;
    private List<BookingDTO> UpcomingBookingRequests;
    private List<BookingDTO> RecentBookingRequests;

    private List<KolBookingCountDTO> topKolsByBookings;
    private List<KolBookingRevenueDTO> topKolsByRevenue;
    private List<BookingStatusCountDTO> statusDistribution;
    private List<PlatformCountDTO> platformDistribution;
    private List<BookingMonthlyTrendDTO> bookingTrend;

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

    @Data
    public static class StatusRequestBreakdownDTO {
        List<ChartDataPointDTO> statusRequestBreakdown;

    }
}
