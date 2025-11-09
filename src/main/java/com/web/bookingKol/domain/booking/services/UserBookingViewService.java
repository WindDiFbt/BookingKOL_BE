package com.web.bookingKol.domain.booking.services;


import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.UserBookingRequestResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserBookingViewService {
    ApiResponse<?> cancelBookingRequest(UUID bookingRequestId, String userEmail);
}

