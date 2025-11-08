package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolDashboardDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public interface KolDashboardService {
    ApiResponse<KolDashboardDTO> getKolSummary(UUID kolId, Instant startDate, Instant endDate);
}
