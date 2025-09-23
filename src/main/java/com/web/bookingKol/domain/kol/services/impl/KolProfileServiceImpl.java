package com.web.bookingKol.domain.kol.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.FilterKolDTO;
import com.web.bookingKol.domain.kol.dtos.KolProfileDTO;
import com.web.bookingKol.domain.kol.mappers.KolProfileMapper;
import com.web.bookingKol.domain.kol.models.KolProfile;
import com.web.bookingKol.domain.kol.repositories.KolProfileRepository;
import com.web.bookingKol.domain.kol.services.KolProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KolProfileServiceImpl implements KolProfileService {
    @Autowired
    private KolProfileRepository kolProfileRepository;
    @Autowired
    private KolProfileMapper kolProfileMapper;

    @Override
    public ApiResponse<KolProfileDTO> getKolProfileByUserId(UUID userId) {
        KolProfile kolProfile = kolProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for userId: " + userId));
        return ApiResponse.<KolProfileDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get kol profile success"))
                .data(kolProfileMapper.toDto(kolProfile))
                .build();
    }

    @Override
    public ApiResponse<KolProfileDTO> getKolProfileByKolId(UUID kolId) {
        KolProfile kolProfile = kolProfileRepository.findByKolId(kolId)
                .orElseThrow(() -> new EntityNotFoundException("KolProfile not found for kolId: " + kolId));
        return ApiResponse.<KolProfileDTO>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get kol profile by kolId success"))
                .data(kolProfileMapper.toDto(kolProfile))
                .build();
    }

    @Override
    public ApiResponse<List<KolProfileDTO>> getAllKolProfiles() {
        List<KolProfileDTO> kolProfileDTOS = kolProfileMapper.toDtoList(kolProfileRepository.findAll());
        return ApiResponse.<List<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol profiles success"))
                .data(kolProfileDTOS)
                .build();
    }

    @Override
    public ApiResponse<List<KolProfileDTO>> getAllKolAvailableProfiles() {
        List<KolProfileDTO> kolProfileDTOS = kolProfileMapper.toDtoList(kolProfileRepository.findAllKolAvailableProfiles());
        return ApiResponse.<List<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol profiles success"))
                .data(kolProfileDTOS)
                .build();
    }

    @Override
    public ApiResponse<List<KolProfileDTO>> getAllKolProfilesByCategoryId(UUID categoryId) {
        List<KolProfileDTO> kolProfileDTOS = kolProfileMapper.toDtoList(kolProfileRepository.findByCategoryId(categoryId));
        return ApiResponse.<List<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol profiles success"))
                .data(kolProfileDTOS)
                .build();
    }

    @Override
    public ApiResponse<List<KolProfileDTO>> getAllKolWithFilter(FilterKolDTO filterKolDTO) {
        Double minRating = filterKolDTO.getMinRating() != null ? filterKolDTO.getMinRating() : null;
        UUID categoryId = filterKolDTO.getCategoryId() != null ? filterKolDTO.getCategoryId() : null;
        Double minPrice = filterKolDTO.getMinBookingPrice() != null ? filterKolDTO.getMinBookingPrice() : null;
        String city = filterKolDTO.getCity() != null ? filterKolDTO.getCity().trim() : null;
        List<KolProfile> kolProfiles = kolProfileRepository.filterKols(minRating, categoryId, minPrice, city);
        if (kolProfiles.isEmpty()) {
            return ApiResponse.<List<KolProfileDTO>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(List.of("No kol profiles found with the given filters"))
                    .data(null)
                    .build();
        }
        List<KolProfileDTO> kolProfileDTOS = kolProfileMapper.toDtoList(kolProfiles);
        return ApiResponse.<List<KolProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all kol profiles success"))
                .data(kolProfileDTOS)
                .build();
    }
}
