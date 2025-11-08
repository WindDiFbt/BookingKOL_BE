package com.web.bookingKol.domain.booking.mappers;

import com.web.bookingKol.domain.booking.dtos.livestreamMetric.LivestreamMetricDTO;
import com.web.bookingKol.domain.booking.models.BookingRequest;
import com.web.bookingKol.domain.booking.models.LivestreamMetric;
import org.springframework.stereotype.Component;

@Component
public class LivestreamMetricMapperV2 {
    public LivestreamMetricDTO toDto(LivestreamMetric entity) {
        if (entity == null) return null;
        LivestreamMetricDTO dto = new LivestreamMetricDTO();
        dto.setId(entity.getId());
        dto.setRevenue(entity.getRevenue());
        if (entity.getKolWorkTime() != null)
            dto.setKolWorkTimeId(entity.getKolWorkTime().getId());
        dto.setLiveViewsOver1min(entity.getLiveViewsOver1min());
        dto.setViewsUnder1min(entity.getViewsUnder1min());
        dto.setCommentsIn1min(entity.getCommentsIn1min());
        dto.setTotalComments(entity.getTotalComments());
        dto.setAddToCartIn1min(entity.getAddToCartIn1min());
        dto.setTotalViews(entity.getTotalViews());
        dto.setAvgViewDuration(entity.getAvgViewDuration());
        dto.setPcu(entity.getPcu());
        dto.setProductClickRate(entity.getProductClickRate());
        dto.setOrderConversionRate(entity.getOrderConversionRate());
        dto.setGpm(entity.getGpm());
        dto.setTotalOrders(entity.getTotalOrders());
        dto.setBuyers(entity.getBuyers());
        dto.setAvgOrderValue(entity.getAvgOrderValue());
        dto.setProductsSold(entity.getProductsSold());
        dto.setIsConfirmed(entity.getIsConfirmed());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setConfirmedAt(entity.getConfirmedAt());
        BookingRequest bookingRequest = entity.getKolWorkTime().getBookingRequest();
        if (bookingRequest != null) {
            dto.setBookingId(bookingRequest.getId());
            dto.setRequestNumber(bookingRequest.getRequestNumber());
        }
        return dto;
    }
}
