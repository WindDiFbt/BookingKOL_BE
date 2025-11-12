package com.web.bookingKol.domain.admin.dashboard.course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueOverviewDTO {
    private Long totalRevenue;
    private Long totalPurchases;
    private Long uniqueUsers;
    private Long notAssignedPurchase;
}
