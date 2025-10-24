package com.web.bookingKol.domain.booking.dtos.livestreamMetric;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LivestreamMetricReqDTO {
    @DecimalMin(value = "0.0", inclusive = true, message = "Doanh thu phải là số không âm")
    private BigDecimal revenue;
    @Min(value = 0, message = "Lượt xem Live > 1 phút phải là số không âm")
    private Integer liveViewsOver1min;

    @Min(value = 0, message = "Lượt xem < 1 phút phải là số không âm")
    private Integer viewsUnder1min;
    @Min(value = 0, message = "Bình luận trong 1 phút phải là số không âm")
    private Integer commentsIn1min;
    @Min(value = 0, message = "Tổng bình luận phải là số không âm")
    private Integer totalComments;
    @Min(value = 0, message = "Thêm vào giỏ hàng trong 1 phút phải là số không âm")
    private Integer addToCartIn1min;
    @Min(value = 0, message = "Tổng lượt xem phải là số không âm")
    private Integer totalViews;
    @Min(value = 0, message = "Thời lượng xem trung bình phải là số dương.")
    private Integer avgViewDuration;
    @Min(value = 0, message = "PCU phải là số không âm")
    private Integer pcu;
    @DecimalMin(value = "0.0", inclusive = true, message = "Tỷ lệ click vào sản phẩm phải là số không âm")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tỷ lệ click vào sản phẩm không được vượt quá 100%")
    private BigDecimal productClickRate;
    @DecimalMin(value = "0.0", inclusive = true, message = "Tỷ lệ chuyển đổi đơn hàng phải là số không âm")
    @DecimalMax(value = "100.0", inclusive = true, message = "Tỷ lệ chuyển đổi đơn hàng không được vượt quá 100%")
    private BigDecimal orderConversionRate;
    @DecimalMin(value = "0.0", inclusive = true, message = "GPM phải là số không âm")
    private BigDecimal gpm;
    @Min(value = 0, message = "Tổng đơn hàng phải là số không âm")
    private Integer totalOrders;
    @Min(value = 0, message = "Số người mua phải là số không âm")
    private Integer buyers;
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá trị đơn hàng trung bình phải là số không âm")
    private BigDecimal avgOrderValue;
    @Min(value = 0, message = "Số mặt hàng được bán phải là số không âm")
    private Integer productsSold;
}
