package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.KolProfileDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses =  {CategoryMapper.class})
public interface KolProfileMapper {
    KolProfileDTO toDto(KolProfile kolProfile);

    List<KolProfileDTO> toDtoList(List<KolProfile> kolProfiles);
}
