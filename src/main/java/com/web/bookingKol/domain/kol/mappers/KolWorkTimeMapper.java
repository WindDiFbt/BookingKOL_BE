package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.models.KolWorkTime;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface KolWorkTimeMapper {
    KolWorkTimeDTO toDto(KolWorkTime kolWorkTime);

    Set<KolWorkTimeDTO> toDtoSet(Set<KolWorkTime> kolWorkTimes);
}
