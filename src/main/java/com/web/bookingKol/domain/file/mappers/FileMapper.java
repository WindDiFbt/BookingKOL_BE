package com.web.bookingKol.domain.file.mappers;

import com.web.bookingKol.domain.file.models.File;
import com.web.bookingKol.domain.file.dtos.FileDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDTO toDto(File file);

    List<FileDTO> toDtoList(List<File> files);
}
