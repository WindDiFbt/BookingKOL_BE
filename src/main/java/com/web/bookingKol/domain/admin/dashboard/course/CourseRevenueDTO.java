package com.web.bookingKol.domain.admin.dashboard.course;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRevenueDTO {
    private String courseName;
    private Long totalSales;
    private Long totalRevenue;
}
