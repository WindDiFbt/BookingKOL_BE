package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolCancelRequestDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface KolCancelRequestService {
    ApiResponse<KolCancelRequestDTO> requestCancelWorkTimeByEmail(String email, UUID workTimeId, String reason);
    ApiResponse<String> approveCancelRequest(UUID requestId, String adminNote);
    ApiResponse<String> rejectCancelRequest(UUID requestId, String adminNote);
}

