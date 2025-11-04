package com.web.bookingKol.domain.booking.services;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.booking.dtos.UserContractResponse;
import org.springframework.data.domain.Pageable;

public interface AdminContractService {
    ApiResponse<PagedResponse<UserContractResponse>> getAllContracts(String keyword, Pageable pageable);
}

