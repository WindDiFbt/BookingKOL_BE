package com.web.bookingKol.domain.user.services;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.BookedPackageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface BookingUserService {

    ApiResponse<PagedResponse<BookedPackageResponse>> getUserBookings(
            String userEmail,
            String search,
            Instant startDate,
            Instant endDate,
            String packageType,
            Pageable pageable
    );

    ApiResponse<?> cancelBookingRequest(UUID id, String userEmail);

}

