package com.web.bookingKol.domain.user.services;

import com.web.bookingKol.common.PagedResponse;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.AdminBookingRequestResponse;
import com.web.bookingKol.domain.user.dtos.AdminCreateBookingRequestDTO;
import com.web.bookingKol.domain.user.dtos.CampaignDetailResponse;
import com.web.bookingKol.domain.user.dtos.UpdateBookingRequestStatusDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface AdminBookingRequestService {
    ApiResponse<?> createBookingRequest(AdminCreateBookingRequestDTO dto, String adminEmail);
    ApiResponse<PagedResponse<AdminBookingRequestResponse>> getAllBookingRequests(Pageable pageable);
    ApiResponse<?> updateBookingRequestStatus(java.util.UUID id, UpdateBookingRequestStatusDTO dto, String adminEmail);

    ApiResponse<AdminBookingRequestResponse> getBookingRequestDetail(UUID bookingRequestId);

    ApiResponse<CampaignDetailResponse> getCampaignDetail(UUID campaignId);

}

