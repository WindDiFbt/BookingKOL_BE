package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.kol.dtos.KolProfileDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, FileUsageMapper.class})
public interface KolProfileMapper {
    @Mapping(target = "fileUsageDtos", source = "fileUsages")
    KolProfileDTO toDto(KolProfile kolProfile);

    @Mapping(target = "fileUsageDtos", source = "fileUsages")
    List<KolProfileDTO> toDtoList(List<KolProfile> kolProfiles);
}
