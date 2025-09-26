package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.kol.dtos.KolOverviewDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KolOverviewMapper {
    @Autowired
    private CategoryMapper categoryMapper;

    public KolOverviewDTO toDto(KolProfile kol) {
        KolOverviewDTO dto = new KolOverviewDTO();
        dto.setId(kol.getId());
        dto.setDisplayName(kol.getDisplayName());
        dto.setBio(kol.getBio());
        dto.setCountry(kol.getCountry());
        dto.setCity(kol.getCity());
        dto.setLanguages(kol.getLanguages());
        dto.setRateCardNote(kol.getRateCardNote());
        dto.setMinBookingPrice(kol.getMinBookingPrice());
        dto.setIsAvailable(kol.getIsAvailable());
        dto.setOverallRating(kol.getOverallRating());
        dto.setFeedbackCount(kol.getFeedbackCount());
        dto.setCategories(categoryMapper.toDtoSet(kol.getCategories()));
        return dto;
    }
}
