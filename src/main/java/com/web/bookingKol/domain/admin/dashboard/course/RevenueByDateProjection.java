package com.web.bookingKol.domain.admin.dashboard.course;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueByDateProjection {
    LocalDate getDate();

    BigDecimal getTotalRevenue();
}
