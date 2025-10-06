package com.web.bookingKol.domain.kol.services;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.models.KolAvailability;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface KolAvailabilityService {
    ApiResponse<List<KolAvailabilityDTO>> getKolSchedule(UUID userId, OffsetDateTime start, OffsetDateTime end);
    ApiResponse<List<KolAvailabilityDTO>> getKolSchedule(UUID userId, OffsetDateTime start, OffsetDateTime end, String status, String note);
    ApiResponse<KolAvailabilityDTO> createAvailability(UUID userId, KolAvailabilityDTO dto);
    ApiResponse<KolAvailabilityDTO> updateAvailability(UUID userId, UUID availabilityId, KolAvailabilityDTO dto);
    ApiResponse<Void> deleteAvailability(UUID userId, UUID availabilityId);
}


