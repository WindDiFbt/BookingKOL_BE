package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserContractService {
    ApiResponse<?> signContract(UUID contractId, UUID bookingRequestId, String userEmail);
    ApiResponse<?> rejectContract(UUID contractId, UUID bookingRequestId, String userEmail, String reason);

    ApiResponse<PagedResponse<UserContractResponse>> getUserContracts(String userEmail, String keyword, Pageable pageable);
}

