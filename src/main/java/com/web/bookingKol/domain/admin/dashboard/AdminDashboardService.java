package com.web.bookingKol.domain.admin.dashboard;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.Contract;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.ContractRepository;
import com.web.bookingKol.domain.booking.repositories.IChartDataPointProjection;
import com.web.bookingKol.domain.course.CoursePackageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {
    private final ContractRepository contractRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final CoursePackageRepository coursePackageRepository;
    private static final int TOP_KOL_LIMIT = 10;
    private static final int DEFAULT_DATA_DAY = 7;

    public AdminDashboardService(ContractRepository contractRepository,
                                 BookingRequestRepository bookingRequestRepository,
                                 CoursePackageRepository coursePackageRepository) {
        this.contractRepository = contractRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.coursePackageRepository = coursePackageRepository;
    }

    public AdminDashboardDTO getDashboard(Instant startDate, Instant endDate) {
        Instant now = Instant.now();
        if (startDate == null) {
            startDate = now.minus(DEFAULT_DATA_DAY, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = now;
        }
        List<String> activeStatusesForRequest = List.of(Enums.BookingStatus.PAID.name(), Enums.BookingStatus.IN_PROGRESS.name(),
                Enums.BookingStatus.COMPLETED.name());
        List<String> activeStatusesForContract = List.of(Enums.ContractStatus.PAID.name(), Enums.ContractStatus.COMPLETED.name());
        List<String> allStatusesForContract = List.of(Enums.ContractStatus.PAID.name(), Enums.ContractStatus.COMPLETED.name(),
                Enums.ContractStatus.REFUNDED.name());
        List<String> notInStatusesForContract = List.of(Enums.ContractStatus.DRAFT.name());

        BigDecimal totalRevenue = contractRepository.getTotalRevenueBetween(startDate, endDate, activeStatusesForContract);
        BigDecimal earnedRevenue = contractRepository.getRevenueBetween(startDate, endDate, Enums.ContractStatus.COMPLETED.name());
        BigDecimal pendingRevenue = contractRepository.getRevenueBetween(startDate, endDate, Enums.ContractStatus.PAID.name());
        BigDecimal cancelledLoss = contractRepository.getRevenueBetween(startDate, endDate, Enums.ContractStatus.REFUNDED.name());

        List<BookingRequest> activeBookings = bookingRequestRepository
                .findAllByKolIdAndStartAtBetweenAndStatusIn(startDate, endDate, activeStatusesForRequest);

        long totalBookings = activeBookings.size();
        long completedBookings = activeBookings.stream()
                .filter(b -> Enums.BookingStatus.COMPLETED.name().equals(b.getStatus())).count();
        long inProgressBookings = activeBookings.stream()
                .filter(b -> Enums.BookingStatus.IN_PROGRESS.name().equals(b.getStatus())).count();

        AdminDashboardDTO.DashboardStatsDTO stats = new AdminDashboardDTO.DashboardStatsDTO(
                totalBookings,
                completedBookings,
                inProgressBookings,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                earnedRevenue != null ? earnedRevenue : BigDecimal.ZERO,
                pendingRevenue != null ? pendingRevenue : BigDecimal.ZERO,
                cancelledLoss != null ? cancelledLoss : BigDecimal.ZERO
        );
        //status contract breakdown
        List<IChartDataPointProjection> statusBreakdownRaw = contractRepository
                .getStatusBreakdown(startDate, endDate, notInStatusesForContract);
        long total = statusBreakdownRaw.stream()
                .mapToLong(p -> p.getValue().longValue())
                .sum();
        List<AdminDashboardDTO.ChartDataPointDTO> statusRequestBreakdown = statusBreakdownRaw
                .stream()
                .map(p -> {
                    Number value = p.getValue();
                    double percentage = (total == 0) ? 0 : ((double) value.longValue() / total) * 100;
                    percentage = Math.round(percentage * 100.0) / 100.0;
                    return new AdminDashboardDTO.ChartDataPointDTO(
                            p.getLabel(),
                            value,
                            percentage
                    );
                })
                .toList();

        List<AdminDashboardDTO.ChartDataPointDTO> revenueChart = contractRepository
                .getRevenueChartData(startDate, endDate)
                .stream()
                .map(p -> new AdminDashboardDTO.ChartDataPointDTO(p.getLabel(), p.getValue(), 0.0))
                .toList();
        AdminDashboardDTO adminDashboardDTO = new AdminDashboardDTO();

        // 1. Lấy Top 10 KOL theo lượt booking
        Pageable top10 = PageRequest.of(0, TOP_KOL_LIMIT);
        List<KolBookingCountDTO> topKolsByBookings = bookingRequestRepository.findTopKolBookingCounts(top10, activeStatusesForRequest, startDate, endDate);
        adminDashboardDTO.setTopKolsByBookings(topKolsByBookings);

        // 2. Lấy Top 10 KOL theo doanh thu
        List<KolBookingRevenueDTO> topKolsByRevenue = contractRepository.findTopKolBookingRevenue(top10, activeStatusesForContract, startDate, endDate);
        adminDashboardDTO.setTopKolsByRevenue(topKolsByRevenue);

        // 4. Lấy phân phối nền tảng
        List<PlatformCountDTO> platformDistribution = bookingRequestRepository.findPlatformCounts(activeStatusesForRequest, startDate, endDate);
        adminDashboardDTO.setPlatformDistribution(platformDistribution);

        // 5. Lấy xu hướng booking (12 tháng qua)
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);
        List<BookingMonthlyTrendDTO> bookingTrend = contractRepository.findBookingMonthlyTrend(oneYearAgo, allStatusesForContract);
        adminDashboardDTO.setBookingTrend(bookingTrend);

        adminDashboardDTO.setStats(stats);
        adminDashboardDTO.setRevenueChart(revenueChart);
        adminDashboardDTO.setStatusContractBreakdown(statusRequestBreakdown);

        List<BookingRequest> recentBookingRequests = bookingRequestRepository
                .findAllByKolIdAndStatusInAndStartAtAfter(
                        Enums.BookingStatus.IN_PROGRESS.name(),
                        startDate,
                        endDate,
                        Sort.by(Sort.Direction.ASC, "startAt")
                );

        List<BookingRequest> upcomingBookingRequests = bookingRequestRepository
                .findAllByKolIdAndStatusInAndStartAtAfter(
                        Enums.BookingStatus.PAID.name(),
                        Instant.now(),
                        Instant.now().plus(DEFAULT_DATA_DAY, ChronoUnit.DAYS),
                        Sort.by(Sort.Direction.ASC, "startAt")
                );

        adminDashboardDTO.setRecentBookingRequests(recentBookingRequests.stream().map(this::mapBookingRequestToDTO).collect(Collectors.toList()));
        adminDashboardDTO.setUpcomingBookingRequests(upcomingBookingRequests.stream().map(this::mapBookingRequestToDTO).collect(Collectors.toList()));

        adminDashboardDTO.setRevenueOverview(coursePackageRepository.getOverview());
        adminDashboardDTO.setCourseRevenue(coursePackageRepository.getRevenueByCourse());
        adminDashboardDTO.setRevenueByDate(coursePackageRepository.getRevenueByDate());

        return adminDashboardDTO;
    }

    public ApiResponse<AdminDashboardDTO> getAdminDashboard(Instant startDate, Instant endDate) {
        return ApiResponse.<AdminDashboardDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy dashboard Admin thành công"))
                .data(getDashboard(startDate, endDate))
                .build();
    }

//    public ApiResponse<?> getSuperAdminDashboard(Instant startDate, Instant endDate) {
//        Instant now = Instant.now();
//        if (startDate == null) {
//            startDate = now.minus(DEFAULT_DATA_DAY, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
//        }
//        if (endDate == null) {
//            endDate = now;
//        }
//
//    }

    private BigDecimal getPrimaryContractAmount(BookingRequest booking) {
        if (booking.getContracts() == null || booking.getContracts().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return booking.getContracts().stream()
                .filter(contract -> contract.getAmount() != null)
                .findFirst()
                .map(Contract::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    private List<AdminDashboardDTO.BookingDTO> mapToUpcomingDTOs(List<BookingRequest> bookingRequests) {
        if (bookingRequests == null || bookingRequests.isEmpty()) {
            return Collections.emptyList();
        }
        return bookingRequests.stream()
                .map(this::mapBookingRequestToDTO)
                .collect(Collectors.toList());
    }

    private AdminDashboardDTO.BookingDTO mapBookingRequestToDTO(BookingRequest bookingRequest) {
        AdminDashboardDTO.BookingDTO dto = new AdminDashboardDTO.BookingDTO();
        if (bookingRequest != null) {
            dto.setBookingId(bookingRequest.getId());
            dto.setRequestNumber(bookingRequest.getRequestNumber());
            dto.setClientName(bookingRequest.getUser().getFullName());
            dto.setStartAt(bookingRequest.getStartAt());
            dto.setEndAt(bookingRequest.getEndAt());
            dto.setLocation(bookingRequest.getLocation());
            dto.setStatus(bookingRequest.getStatus());
        }
        return dto;
    }
}
