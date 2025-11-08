package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolDashboardDTO;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolFeedback;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolAvailabilityRepository;
import com.web.bookingKol.domain.kol.repositories.KolFeedbackRepository;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.kol.services.KolDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KolDashboardServiceImpl implements KolDashboardService {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private KolAvailabilityRepository kolAvailabilityRepository;
    @Autowired
    private KolFeedbackRepository kolFeedbackRepository;

    @Override
    public ApiResponse<KolDashboardDTO> getKolSummary(UUID kolId, Instant startDate, Instant endDate) {
        Instant now = Instant.now();
        if (startDate == null) {
            startDate = now.minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = now;
        }
        List<String> activeStatusesForWorkTime = List.of(Enums.KOLWorkTimeStatus.IN_PROGRESS.name(),
                Enums.KOLWorkTimeStatus.COMPLETED.name());
        List<KolWorkTime> activeWorkTimes = kolWorkTimeRepository
                .findAllByKolIdAndStartAtBetweenAndStatusIn(kolId, startDate, endDate, activeStatusesForWorkTime);

        List<KolWorkTime> completedWorkTimes = activeWorkTimes.stream()
                .filter(b -> Enums.KOLWorkTimeStatus.COMPLETED.name().equals(b.getStatus())).toList();
        List<KolWorkTime> recentWorkTimes = activeWorkTimes.stream()
                .filter(b -> Enums.KOLWorkTimeStatus.IN_PROGRESS.name().equals(b.getStatus())).toList();
        List<KolWorkTime> upcomingWorkTimes = kolWorkTimeRepository
                .findAllByKolIdAndStatusInAndStartAtAfter(
                        kolId,
                        Enums.KOLWorkTimeStatus.IN_PROGRESS.name(),
                        Instant.now(),
                        Instant.now().plus(7, ChronoUnit.DAYS),
                        Sort.by(Sort.Direction.ASC, "startAt")
                );
        List<KolAvailability> availabilities = kolAvailabilityRepository
                .findAvailabilities(kolId, startDate, endDate);

        double totalMinutesAvailable = availabilities.stream()
                .mapToLong(a -> Duration.between(a.getStartAt(), a.getEndAt()).toMinutes()).sum();
        double totalHoursAvailable = totalMinutesAvailable / 60.0;
        int totalWorkTimeCount = activeWorkTimes.size();
        int completedWorkTimeCount = completedWorkTimes.size();
        int recentWorkTimesCount = recentWorkTimes.size();
        int upcomingWorkTimesCount = upcomingWorkTimes.size();

        KolDashboardDTO.DashboardStatsDTO stats = new KolDashboardDTO.DashboardStatsDTO(
                totalWorkTimeCount,
                completedWorkTimeCount,
                recentWorkTimesCount,
                upcomingWorkTimesCount
        );

        long totalMinutesCompleted = completedWorkTimes.stream()
                .mapToLong(wt -> Duration.between(wt.getStartAt(), wt.getEndAt()).toMinutes()).sum();
        double totalHoursCompleted = totalMinutesCompleted / 60.0;

        long totalMinutesRecent = recentWorkTimes.stream()
                .mapToLong(wt -> Duration.between(wt.getStartAt(), wt.getEndAt()).toMinutes()).sum();

        long totalMinutesUpcoming = upcomingWorkTimes.stream()
                .mapToLong(wt -> Duration.between(wt.getStartAt(), wt.getEndAt()).toMinutes()).sum();
        double totalHoursUpcoming = totalMinutesUpcoming / 60.0;

        double totalMinutesBooked = totalMinutesCompleted + totalMinutesRecent;
        double totalHoursBooked = totalMinutesBooked / 60.0;
        double utilizationRate = (totalMinutesAvailable > 0) ? (totalMinutesBooked / totalMinutesAvailable) : 0.0;

        long durationDay = Duration.between(startDate, endDate).toDays();
        double averageTimeAvailablePerDay = totalHoursAvailable / durationDay;
        double averageWorkTimePerDay = totalHoursBooked / durationDay;
        KolDashboardDTO.TimeAndValueStatsDTO timeAndValueStats = new KolDashboardDTO.TimeAndValueStatsDTO(
                totalHoursAvailable,
                totalHoursCompleted,
                totalHoursUpcoming,
                utilizationRate * 100.0,
                Math.round(averageTimeAvailablePerDay * 100.0) / 100.0,
                Math.round(averageWorkTimePerDay * 100.0) / 100.0
        );

        //FeedbackStats
        List<KolFeedback> feedbacks = kolFeedbackRepository
                .findByKolIdAndDateRangeWithReviewer(kolId, startDate, endDate);
        long totalReviews = feedbacks.size();
        KolDashboardDTO.FeedbackStatsDTO feedbackStats;
        if (totalReviews == 0) {
            feedbackStats = new KolDashboardDTO.FeedbackStatsDTO(0.0, 0L, 0.0, 0.0, 0.0, 0.0, 0.0);
        } else {
            double avgOverall = feedbacks.stream()
                    .mapToDouble(KolFeedback::getOverallRating)
                    .average().orElse(0.0);
            long rehireCount = feedbacks.stream()
                    .filter(f -> f.getWouldRehire() != null && f.getWouldRehire())
                    .count();
            double rehireRate = ((double) rehireCount / totalReviews) * 100.0;
            double avgProf = feedbacks.stream()
                    .filter(f -> f.getProfessionalismRating() != null)
                    .mapToInt(KolFeedback::getProfessionalismRating)
                    .average().orElse(0.0);
            double avgComm = feedbacks.stream()
                    .filter(f -> f.getCommunicationRating() != null)
                    .mapToInt(KolFeedback::getCommunicationRating)
                    .average().orElse(0.0);
            double avgTimeline = feedbacks.stream()
                    .filter(f -> f.getTimelineRating() != null)
                    .mapToInt(KolFeedback::getTimelineRating)
                    .average().orElse(0.0);
            double avgQuality = feedbacks.stream()
                    .filter(f -> f.getContentQualityRating() != null)
                    .mapToInt(KolFeedback::getContentQualityRating)
                    .average().orElse(0.0);
            feedbackStats = new KolDashboardDTO.FeedbackStatsDTO(
                    avgOverall,
                    totalReviews,
                    rehireRate,
                    avgProf,
                    avgComm,
                    avgTimeline,
                    avgQuality
            );
        }

        List<KolDashboardDTO.WorkTimeDTO> upcomingDTOs = mapToUpcomingDTOs(upcomingWorkTimes);
        List<KolDashboardDTO.WorkTimeDTO> recentDTOs = recentWorkTimes.stream().map(this::mapWorkTimeToDTO).collect(Collectors.toList());
        List<KolDashboardDTO.WorkTimeDTO> completedDTOs = completedWorkTimes.stream().map(this::mapWorkTimeToDTO).collect(Collectors.toList());

        KolDashboardDTO dashboard = new KolDashboardDTO();
        dashboard.setStats(stats);
        dashboard.setTimeAndValue(timeAndValueStats);
        dashboard.setFeedbackStats(feedbackStats);
        dashboard.setUpcomingWorkTimes(upcomingDTOs);
        dashboard.setRecentWorkTimes(recentDTOs);
        dashboard.setCompletedWorkTimes(completedDTOs);

        return ApiResponse.<KolDashboardDTO>builder()
                .status(200)
                .message(List.of("Lấy tổng quan Kol thành công!"))
                .data(dashboard)
                .build();
    }

    private List<KolDashboardDTO.WorkTimeDTO> mapToUpcomingDTOs(List<KolWorkTime> kolWorkTimes) {
        if (kolWorkTimes == null || kolWorkTimes.isEmpty()) {
            return Collections.emptyList();
        }
        return kolWorkTimes.stream()
                .map(this::mapWorkTimeToDTO)
                .collect(Collectors.toList());
    }

    private List<KolDashboardDTO.WorkTimeDTO> mapToRecentDTOs(List<KolWorkTime> kolWorkTimes, String status) {
        if (kolWorkTimes == null || kolWorkTimes.isEmpty()) {
            return Collections.emptyList();
        }
        return kolWorkTimes.stream()
                .filter(workTime -> status.equals(workTime.getStatus()))
                .map(this::mapWorkTimeToDTO)
                .collect(Collectors.toList());
    }

    private KolDashboardDTO.WorkTimeDTO mapWorkTimeToDTO(KolWorkTime workTime) {
        KolDashboardDTO.WorkTimeDTO dto = new KolDashboardDTO.WorkTimeDTO();
        if (workTime.getBookingRequest() != null) {
            dto.setBookingId(workTime.getBookingRequest().getId());
            dto.setRequestNumber(workTime.getBookingRequest().getRequestNumber());
        }
        dto.setKolWorkTimeId(workTime.getId());
        dto.setStartAt(workTime.getStartAt());
        dto.setEndAt(workTime.getEndAt());
        dto.setStatus(workTime.getStatus());
        return dto;
    }
}
