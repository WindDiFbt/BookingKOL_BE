package com.web.bookingKol.domain.user.services;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.UserBookingRequestResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserBookingRequestService {
    ApiResponse<PagedResponse<UserBookingRequestResponse>> getUserBookingRequests(String userEmail, Pageable pageable);
    ApiResponse<UserBookingRequestResponse> getBookingRequestDetail(UUID id, String userEmail);
}

