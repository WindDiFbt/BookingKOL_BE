package com.web.bookingKol.domain.course;

import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {FileUsageMapper.class})
public interface CoursePackageMapper {
    @Mapping(target = "fileUsageDtos", source = "fileUsages")
    CoursePackageDTO toDto(CoursePackage coursePackage);

    @Mapping(target = "fileUsageDtos", source = "fileUsages")
    List<CoursePackageDTO> toDtoList(List<CoursePackage> coursePackages);
}
