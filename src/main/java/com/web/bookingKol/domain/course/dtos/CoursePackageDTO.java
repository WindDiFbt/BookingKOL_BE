package com.web.bookingKol.domain.course.dtos;

import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CoursePackageDTO {
    private UUID id;
    @NotNull
    private String name;
    @NotNull
    private Long price;
    private Long currentPrice;
    private Integer discount;
    private String description;
    private Boolean isAvailable;

    private Set<FileUsageDTO> fileUsageDtos;
}
