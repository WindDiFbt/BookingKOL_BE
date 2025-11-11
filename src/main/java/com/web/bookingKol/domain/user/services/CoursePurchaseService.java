package com.web.bookingKol.domain.user.services;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.PurchasedCourseResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface CoursePurchaseService {
    ApiResponse<PagedResponse<PurchasedCourseResponse>> getPurchaseHistoryUser(
            UUID userId,
            String search,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );

    ApiResponse<PagedResponse<PurchasedCourseResponse>> getPurchaseHistoryAdmin(
            String search,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );

    ApiResponse<?> confirmPurchasedCourse(UUID purchasedCourseId);
}


