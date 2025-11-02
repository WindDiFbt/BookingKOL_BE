package com.web.bookingKol.domain.kol.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.kol.dtos.KolLeaveRequestDTO;
import org.springframework.data.domain.Page;


import java.util.UUID;

public interface KolLeaveRequestService {
    ApiResponse<KolLeaveRequestDTO> requestLeave(UUID kolId, UUID availabilityId, String reason);
    ApiResponse<String> approveLeave(UUID leaveRequestId, String adminNote);
    ApiResponse<String> rejectLeave(UUID leaveRequestId, String adminNote);
    ApiResponse<Page<KolLeaveRequestDTO>> getMyLeaveRequests(UUID userId, int page, int size, String keyword);
    ApiResponse<Page<KolLeaveRequestDTO>> getAllLeaveRequestsForAdmin(int page, int size, String keyword, String status);


}

