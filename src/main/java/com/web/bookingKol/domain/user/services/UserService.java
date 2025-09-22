package com.web.bookingKol.domain.user.services;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    ApiResponse<List<UserDTO>> getAllUser();
}
