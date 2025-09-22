package com.web.bookingKol.auth;

import com.web.bookingKol.auth.dtos.LoginRequestDTO;
import com.web.bookingKol.auth.dtos.LoginResponseDTO;
import com.web.bookingKol.auth.dtos.RegisterRequestDTO;
import com.web.bookingKol.common.exception.RoleNotFoundException;
import com.web.bookingKol.common.exception.UserAlreadyExistsException;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.models.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<ApiResponse<?>> login(LoginRequestDTO loginRequestDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getIdentifier(), loginRequestDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        String accessToken = jwtUtils.generateAccessToken(authentication);
        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .id(String.valueOf(userDetails.getId()))
                .accessToken(accessToken)
                .type("Bearer")
                .roles(roles)
                .build();
        return ResponseEntity.ok().body(
                ApiResponse.builder().status(response.getStatus()).message(List.of("Login success")).data(loginResponseDTO).build()
        );
    }

    @Override
    public ResponseEntity<ApiResponse<?>> register(RegisterRequestDTO request) throws UserAlreadyExistsException {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> verifyEmail(String email, String code) throws RoleNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> resendEmailVerificationCode(String email) {
        return null;
    }
}
