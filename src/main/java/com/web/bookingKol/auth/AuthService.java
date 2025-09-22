package com.web.bookingKol.auth;

import com.web.bookingKol.common.exception.RoleNotFoundException;
import com.web.bookingKol.common.exception.UserAlreadyExistsException;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.auth.dtos.LoginRequestDTO;
import com.web.bookingKol.auth.dtos.RegisterRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ApiResponse<?>> login(LoginRequestDTO loginRequestDTO, HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<ApiResponse<?>> register(RegisterRequestDTO request) throws UserAlreadyExistsException;

    ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request);

    ResponseEntity<ApiResponse<?>> refreshAccessToken(String refreshToken);

    ResponseEntity<ApiResponse<?>> verifyEmail(String email, String code) throws RoleNotFoundException;

    ResponseEntity<ApiResponse<?>> resendEmailVerificationCode(String email);
}
