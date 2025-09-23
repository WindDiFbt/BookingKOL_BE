package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.PlatformDTO;
import com.web.bookingKol.domain.kol.models.Platform;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlatformMapper {
    PlatformDTO toDto(Platform platform);

    List<PlatformDTO> toDtoList(List<Platform> platforms);
}
