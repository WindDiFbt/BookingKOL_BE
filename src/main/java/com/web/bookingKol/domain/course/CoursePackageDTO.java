package com.web.bookingKol.domain.course;

import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CoursePackageDTO {
    private UUID id;
    private String name;
    private Integer price;
    private Integer discount;
    private String description;

    private Set<FileUsageDTO> fileUsageDtos;
}
