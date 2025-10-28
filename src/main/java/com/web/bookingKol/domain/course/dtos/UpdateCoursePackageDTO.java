package com.web.bookingKol.domain.course.dtos;

import lombok.Data;

@Data
public class UpdateCoursePackageDTO {
    private String name;
    private Long price;
    private Integer discount;
    private String description;
    private Boolean isAvailable;
}
