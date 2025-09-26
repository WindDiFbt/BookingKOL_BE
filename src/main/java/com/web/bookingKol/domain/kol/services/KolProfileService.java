package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.FilterKolDTO;
import com.web.bookingKol.domain.kol.dtos.KolProfileDTO;
import com.web.bookingKol.domain.kol.dtos.KolOverviewDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface KolProfileService {
    ApiResponse<KolProfileDTO> getKolProfileByUserId(UUID userId);

    ApiResponse<KolProfileDTO> getKolProfileByKolId(UUID kolId);

    ApiResponse<List<KolOverviewDTO>> getAllKolProfiles();

    ApiResponse<List<KolOverviewDTO>> getAllKolAvailableProfiles();

    ApiResponse<List<KolOverviewDTO>> getAllKolProfilesByCategoryId(UUID categoryId);

    ApiResponse<List<KolOverviewDTO>> getAllKolWithFilter(FilterKolDTO filterKolDTO);
}
