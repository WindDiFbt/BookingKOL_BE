package com.web.bookingKol.domain.user.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.AdminAccountDTO;
import com.web.bookingKol.domain.user.dtos.UserDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    ApiResponse<?> updateUserStatus(UUID userId, String status);

    ApiResponse<?> createAdminAccount(AdminAccountDTO adminAccountDTO);

    ApiResponse<Page<UserDetailDTO>> getAllAccountWithFilter(Pageable pageable, String status, String role);
}

