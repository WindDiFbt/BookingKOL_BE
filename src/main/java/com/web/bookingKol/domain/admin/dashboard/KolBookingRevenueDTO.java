package com.web.bookingKol.domain.admin.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public interface KolBookingRevenueDTO {
    UUID getKolId();

    String getKolName();

    BigDecimal getTotalRevenue();
}
