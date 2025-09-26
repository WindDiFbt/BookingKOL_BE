package com.web.bookingKol.domain.file.mappers;

import com.web.bookingKol.domain.file.models.FileUsage;
import com.web.bookingKol.domain.file.dtos.FileUsageDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileUsageMapper {
    FileUsageDTO toDto(FileUsage fileUsage);

    List<FileUsageDTO> toDtoList(List<FileUsage> fileUsages);
}
