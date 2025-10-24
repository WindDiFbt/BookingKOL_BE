package com.web.bookingKol.domain.kol.services;


import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolAvailabilityDTO;
import com.web.bookingKol.domain.kol.dtos.TimeSlotDTO;
import com.web.bookingKol.domain.kol.models.KolAvailability;
import com.web.bookingKol.domain.kol.models.KolWorkTimeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public interface KolAvailabilityService {
    ApiResponse<List<KolAvailabilityDTO>> getKolSchedule(UUID userId, Instant start, Instant end);

    ApiResponse<KolAvailabilityDTO> createKolSchedule(UUID kolId, KolAvailabilityDTO availabilityDTO);

    ApiResponse<KolAvailabilityDTO> getKolAvailabilityById(UUID availabilityId);

    ApiResponse<List<KolAvailabilityDTO>> getKolAvailabilitiesByKol(
            UUID kolId,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    );

    ApiResponse<List<TimeSlotDTO>> getKolFreeTimes(
            UUID kolId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );

    ApiResponse<KolWorkTimeDTO> updateKolWorkTimeByAdmin(UUID workTimeId, KolWorkTimeDTO dto);

    ApiResponse<KolAvailabilityDTO> createKolScheduleByAdmin(KolAvailabilityDTO dto);


}