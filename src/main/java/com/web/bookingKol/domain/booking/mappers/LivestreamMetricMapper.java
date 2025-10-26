package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricDTO;
import com.web.bookingKol.domain.booking.models.LivestreamMetric;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LivestreamMetricMapper {
    LivestreamMetricDTO toDto(LivestreamMetric livestreamMetric);

    List<LivestreamMetricDTO> toDtoList(List<LivestreamMetric> livestreamMetrics);
}
