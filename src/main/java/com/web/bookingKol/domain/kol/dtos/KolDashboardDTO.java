package com.web.bookingKol.domain.kol.dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class KolDashboardDTO {
    private DashboardStatsDTO stats;
    private TimeAndValueStatsDTO timeAndValue;
    private FeedbackStatsDTO feedbackStats;
    private List<WorkTimeDTO> UpcomingWorkTimes;
    private List<WorkTimeDTO> RecentWorkTimes;
    private List<WorkTimeDTO> completedWorkTimes;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DashboardStatsDTO {
        private int totalWorkTimesCount;
        private int completedWorkTimesCount;
        private int recentWorkTimesCount;
        private int upcomingWorkTimesCount;
    }

    @Getter
    @Setter
    public static class WorkTimeDTO {
        private UUID bookingId;
        private String requestNumber;
        private UUID kolWorkTimeId;
        private Instant startAt;
        private Instant endAt;
        private String status;
    }

    @Data
    @AllArgsConstructor
    public static class TimeAndValueStatsDTO {
        private double totalHoursAvailable;
        private double totalHoursCompleted;     // (Mới) Tổng số giờ đã làm việc
        private double totalHoursUpcoming;      // (Mới) Tổng số giờ sắp làm việc
        private double utilizationRate;         // (Mới) Tỷ lệ lấp đầy (0.0 - 1.0)
        private double averageTimeAvailablePerDay;
        private double averageWorkTimePerDay;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackStatsDTO {
        private double averageOverallRating; // Trung bình tổng thể (ví dụ: 4.8)
        private long totalReviews;           // Tổng số lượng đánh giá
        private double rehireRate;           // Tỷ lệ % "wouldRehire" = true
        private double averageProfessionalismRating; // Trung bình về sự chuyên nghiệp
        private double averageCommunicationRating; // Trung bình về giao tiếp
        private double averageTimelineRating;      // Trung bình về đúng hạn
        private double averageContentQualityRating;// Trung bình về chất lượng nội dung
    }
}
