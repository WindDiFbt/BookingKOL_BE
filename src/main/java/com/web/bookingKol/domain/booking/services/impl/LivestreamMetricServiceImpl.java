package com.web.bookingKol.domain.booking.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricDTO;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricReqDTO;
import com.web.bookingKol.domain.booking.mappers.LivestreamMetricMapper;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.LivestreamMetric;
import com.web.bookingKol.domain.booking.repositories.BookingRequestRepository;
import com.web.bookingKol.domain.booking.repositories.LivestreamMetricRepository;
import com.web.bookingKol.domain.booking.services.LivestreamMetricService;
import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.repositories.KolWorkTimeRepository;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class LivestreamMetricServiceImpl implements LivestreamMetricService {
    @Autowired
    private KolWorkTimeRepository kolWorkTimeRepository;
    @Autowired
    private LivestreamMetricRepository livestreamMetricRepository;
    @Autowired
    private LivestreamMetricMapper livestreamMetricMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Override
    public ApiResponse<LivestreamMetricDTO> createLivestreamMetric(UUID kolId, UUID workTimeId, LivestreamMetricReqDTO livestreamMetricReqDTO) {
        KolWorkTime kolWorkTime = kolWorkTimeRepository.findById(workTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc của Kol: " + workTimeId));
        if (kolWorkTime.getAvailability().getKol().getId() != kolId) {
            throw new IllegalArgumentException("Ca làm việc " + workTimeId + " không thuộc về KOL " + kolId);
        }
        if (kolWorkTime.getStatus().equals(Enums.KOLWorkTimeStatus.COMPLETED.name())) {
            throw new IllegalArgumentException("Ca làm việc " + workTimeId + " đã hoàn thành.");
        }
        if (kolWorkTime.getStartAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Ca làm việc " + workTimeId + " chưa bắt đầu.");
        }
        if (kolWorkTime.getEndAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Ca làm việc " + workTimeId + " chưa kết thúc.");
        }
        Instant metricDeadline = kolWorkTime.getEndAt().plus(3, ChronoUnit.DAYS);
        if (Instant.now().isAfter(metricDeadline)) {
            throw new IllegalArgumentException("Đã quá thời hạn 3 ngày kể từ khi ca làm việc " + workTimeId + " kết thúc. Không thể tạo Livestream Metric.");
        }
        LivestreamMetric livestreamMetric = new LivestreamMetric();
        livestreamMetric.setRevenue(livestreamMetricReqDTO.getRevenue());
        livestreamMetric.setLiveViewsOver1min(livestreamMetricReqDTO.getLiveViewsOver1min());
        livestreamMetric.setViewsUnder1min(livestreamMetricReqDTO.getViewsUnder1min());
        livestreamMetric.setCommentsIn1min(livestreamMetricReqDTO.getCommentsIn1min());
        livestreamMetric.setTotalComments(livestreamMetricReqDTO.getTotalComments());
        livestreamMetric.setAddToCartIn1min(livestreamMetricReqDTO.getAddToCartIn1min());
        livestreamMetric.setTotalViews(livestreamMetricReqDTO.getTotalViews());
        livestreamMetric.setAvgViewDuration(livestreamMetricReqDTO.getAvgViewDuration());
        livestreamMetric.setPcu(livestreamMetricReqDTO.getPcu());
        livestreamMetric.setProductClickRate(livestreamMetricReqDTO.getProductClickRate());
        livestreamMetric.setOrderConversionRate(livestreamMetricReqDTO.getOrderConversionRate());
        livestreamMetric.setGpm(livestreamMetricReqDTO.getGpm());
        livestreamMetric.setTotalOrders(livestreamMetricReqDTO.getTotalOrders());
        livestreamMetric.setBuyers(livestreamMetricReqDTO.getBuyers());
        livestreamMetric.setAvgOrderValue(livestreamMetricReqDTO.getAvgOrderValue());
        livestreamMetric.setProductsSold(livestreamMetricReqDTO.getProductsSold());
        livestreamMetric.setKolWorkTime(kolWorkTime);
        livestreamMetric.setCreatedAt(Instant.now());
        livestreamMetric.setIsConfirmed(false);

        livestreamMetricRepository.save(livestreamMetric);
        return ApiResponse.<LivestreamMetricDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo Livestream Metric thành công!"))
                .data(livestreamMetricMapper.toDto(livestreamMetric))
                .build();
    }

    @Override
    public ApiResponse<LivestreamMetricDTO> confirmLivestreamMetric(UUID userId, UUID workTimeId) {
        KolWorkTime kolWorkTime = kolWorkTimeRepository.findById(workTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca làm việc của Kol: " + workTimeId));
        if (!kolWorkTime.getStatus().equals(Enums.KOLWorkTimeStatus.IN_PROGRESS.name())) {
            throw new IllegalArgumentException("Ca làm việc " + workTimeId + " đã hoàn thành hoặc không còn trong quá trình làm việc.");
        }
        LivestreamMetric livestreamMetric = kolWorkTime.getLivestreamMetrics();
        if (livestreamMetric.getIsConfirmed()) {
            throw new IllegalArgumentException("Livestream Metric của phiên live trên đã được xác nhận.");
        }
        if (!livestreamMetric.getKolWorkTime().getBookingRequest().getUser().getId().equals(userId)) {
            throw new AuthorizationServiceException("Bạn không có quyền xác nhận Livestream Metric của phiên live làm này.");
        }
        Instant metricDeadline = livestreamMetric.getKolWorkTime().getEndAt().plus(3, ChronoUnit.DAYS);
        if (Instant.now().isAfter(metricDeadline)) {
            throw new IllegalArgumentException("Đã quá thời hạn 3 ngày kể từ khi ca làm việc kết thúc. Không thể xác nhận Livestream Metric.");
        }
        livestreamMetric.setConfirmedAt(Instant.now());
        livestreamMetric.setIsConfirmed(true);
        livestreamMetricRepository.save(livestreamMetric);
        kolWorkTime.setStatus(Enums.KOLWorkTimeStatus.COMPLETED.name());
        kolWorkTimeRepository.save(kolWorkTime);
        completeRequest(livestreamMetric);
        return ApiResponse.<LivestreamMetricDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xác nhận Livestream Metric thành công!"))
                .data(livestreamMetricMapper.toDto(livestreamMetric))
                .build();
    }

    @Override
    public ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetric(UUID userId, Integer livestreamMetricId) {
        LivestreamMetric livestreamMetric = livestreamMetricRepository.findById(livestreamMetricId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Livestream Metric: " + livestreamMetricId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + userId));
        UUID bookingUserId = livestreamMetric.getKolWorkTime().getBookingRequest().getUser().getId();
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals(Enums.Roles.ADMIN.name()));
        if (!isAdmin && !bookingUserId.equals(userId)) {
            throw new AuthorizationServiceException("Bạn không có quyền xem Livestream Metric của ca làm này.");
        }
        return ApiResponse.<LivestreamMetricDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy dữ liệu của phiên live thành công: " + livestreamMetricId))
                .data(livestreamMetricMapper.toDto(livestreamMetric))
                .build();
    }

    @Override
    public ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetricByKolWorkTimeId(UUID userId, UUID workTimeId) {
        LivestreamMetric livestreamMetric = livestreamMetricRepository.findByWorkTimeId(workTimeId);
        if (livestreamMetric == null) {
            throw new IllegalArgumentException("Không tìm thấy Livestream Metric của ca làm việc: " + workTimeId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + userId));
        UUID bookingUserId = livestreamMetric.getKolWorkTime().getBookingRequest().getUser().getId();
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals(Enums.Roles.ADMIN.name()));
        if (!isAdmin && !bookingUserId.equals(userId)) {
            throw new AuthorizationServiceException("Bạn không có quyền xem Livestream Metric của ca làm này.");
        }
        return ApiResponse.<LivestreamMetricDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy dữ liệu của phiên live thành công: " + workTimeId))
                .data(livestreamMetricMapper.toDto(livestreamMetric))
                .build();
    }

    @Override
    public ApiResponse<List<LivestreamMetricDTO>> getLivestreamMetricOfKol(UUID kolId) {
        List<LivestreamMetric> livestreamMetrics = livestreamMetricRepository.findAllByKolId(kolId);
        return ApiResponse.<List<LivestreamMetricDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy dữ liệu livestream của Kol thành công:" + kolId))
                .data(livestreamMetricMapper.toDtoList(livestreamMetrics))
                .build();
    }

    private void completeRequest(LivestreamMetric livestreamMetric) {
        BookingRequest bookingRequest = livestreamMetric.getKolWorkTime().getBookingRequest();
        boolean allWorkTimesCompleted = bookingRequest.getKolWorkTimes().stream()
                .allMatch(kolWorkTime -> kolWorkTime.getStatus().equals(Enums.KOLWorkTimeStatus.COMPLETED.name()));
        if (allWorkTimesCompleted) {
            bookingRequest.setStatus(Enums.BookingStatus.COMPLETED.name());
            bookingRequestRepository.save(bookingRequest);
        }
    }
}
