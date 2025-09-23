package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.FilterKolDTO;
import com.web.bookingKol.domain.kol.dtos.KolProfileDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface KolProfileService {
    ApiResponse<KolProfileDTO> getKolProfileByUserId(UUID userId);

    ApiResponse<KolProfileDTO> getKolProfileByKolId(UUID kolId);

    ApiResponse<List<KolProfileDTO>> getAllKolProfiles();

    ApiResponse<List<KolProfileDTO>> getAllKolAvailableProfiles();

    ApiResponse<List<KolProfileDTO>> getAllKolProfilesByCategoryId(UUID categoryId);

    ApiResponse<List<KolProfileDTO>> getAllKolWithFilter(FilterKolDTO filterKolDTO);
}
