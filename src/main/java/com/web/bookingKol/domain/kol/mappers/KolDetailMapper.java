package com.web.bookingKol.domain.kol.mappers;

import com.web.bookingKol.domain.file.mappers.FileUsageMapper;
import com.web.bookingKol.domain.kol.dtos.KolDetailDTO;
import com.web.bookingKol.domain.kol.models.KolProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KolDetailMapper {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FileUsageMapper fileUsageMapper;

    public KolDetailDTO toDto(KolProfile kol) {
        if (kol == null) return null;
        KolDetailDTO dto = new KolDetailDTO();
        dto.setId(kol.getId());
        dto.setUserId(kol.getUser() != null ? kol.getUser().getId() : null);
        dto.setFullName(kol.getUser() != null ? kol.getUser().getFullName() : null);
        dto.setDisplayName(kol.getDisplayName());
        dto.setAvatarUrl(kol.getUser() != null ? kol.getUser().getAvatarUrl() : null);
        dto.setDob(kol.getDob());
        dto.setBio(kol.getBio());
        dto.setExperience(kol.getExperience());
        dto.setCountry(kol.getCountry());
        dto.setCity(kol.getCity());
        dto.setLanguages(kol.getLanguages());
        dto.setRateCardNote(kol.getRateCardNote());
        dto.setMinBookingPrice(kol.getMinBookingPrice());
        dto.setIsAvailable(kol.getIsAvailable());
        dto.setCreatedAt(kol.getCreatedAt());
        dto.setUpdatedAt(kol.getUpdatedAt());
        dto.setDeletedAt(kol.getDeletedAt());
        dto.setOverallRating(kol.getOverallRating());
        dto.setFeedbackCount(kol.getFeedbackCount());
        dto.setRole(kol.getRole());
        dto.setCategories(categoryMapper.toDtoSet(kol.getCategories()));

        if (kol.getFileUsages() != null) {
            dto.setFileUsageDtos(fileUsageMapper.toDtoSet(kol.getFileUsages()));
        }

        return dto;
    }

    public KolDetailDTO toDtoBasicInformation(KolProfile kol) {
        if (kol == null) return null;
        KolDetailDTO dto = new KolDetailDTO();
        dto.setId(kol.getId());
        dto.setUserId(kol.getUser() != null ? kol.getUser().getId() : null);
        dto.setFullName(kol.getUser() != null ? kol.getUser().getFullName() : null);
        dto.setDisplayName(kol.getDisplayName());
        dto.setAvatarUrl(kol.getUser() != null ? kol.getUser().getAvatarUrl() : null);
        dto.setDob(kol.getDob());
        dto.setBio(kol.getBio());
        dto.setExperience(kol.getExperience());
        dto.setCountry(kol.getCountry());
        dto.setCity(kol.getCity());
        dto.setLanguages(kol.getLanguages());
        dto.setRateCardNote(kol.getRateCardNote());
        dto.setMinBookingPrice(kol.getMinBookingPrice());
        dto.setIsAvailable(kol.getIsAvailable());
        dto.setCreatedAt(kol.getCreatedAt());
        dto.setUpdatedAt(kol.getUpdatedAt());
        dto.setDeletedAt(kol.getDeletedAt());
        dto.setOverallRating(kol.getOverallRating());
        dto.setFeedbackCount(kol.getFeedbackCount());
        return dto;
    }
}
