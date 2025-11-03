package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricDTO;
import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricReqDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface LivestreamMetricService {
    ApiResponse<LivestreamMetricDTO> createLivestreamMetric(UUID kolId, UUID workTimeId, LivestreamMetricReqDTO livestreamMetricReqDTO);

    ApiResponse<LivestreamMetricDTO> confirmLivestreamMetric(UUID userId, UUID workTimeId);

    ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetric(UUID userId, Integer livestreamMetricId);

    ApiResponse<LivestreamMetricDTO> getDetailLivestreamMetricByKolWorkTimeId(UUID userId, UUID workTimeId);

    ApiResponse<Page<LivestreamMetricDTO>> getLivestreamMetricOfKol(UUID kolId, Pageable pageable);

    ApiResponse<LivestreamMetricDTO> KolGetDetailLivestreamMetricByKolWorkTimeId(UUID userId, UUID workTimeId);

}
