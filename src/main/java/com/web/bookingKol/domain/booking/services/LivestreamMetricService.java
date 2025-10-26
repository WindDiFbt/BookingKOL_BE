package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricDTO;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricReqDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface LivestreamMetricService {
    ApiResponse<LivestreamMetricDTO> createLivestreamMetric(UUID kolId, UUID workTimeId, LivestreamMetricReqDTO livestreamMetricReqDTO);

    ApiResponse<LivestreamMetricDTO> confirmLivestreamMetric(UUID userId, UUID workTimeId);

    ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetric(UUID userId, Integer livestreamMetricId);

    ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetricByKolWorkTimeId(UUID userId, UUID workTimeId);

    ApiResponse<List<LivestreamMetricDTO>> getLivestreamMetricOfKol(UUID kolId);
}
