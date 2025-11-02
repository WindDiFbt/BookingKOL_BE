package com.web.bookingKol.auth;

import com.web.bookingKol.auth.dtos.LoginRequestDTO;
import com.web.bookingKol.auth.dtos.LoginResponseDTO;
import com.web.bookingKol.auth.dtos.RegisterRequestDTO;
import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.exception.RoleNotFoundException;
import com.web.bookingKol.common.exception.UserAlreadyExistsException;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.models.*;
import com.web.bookingKol.domain.user.repositories.*;
import com.web.bookingKol.domain.user.models.Brand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.web.bookingKol.common.Enums.UserStatus;

import com.web.bookingKol.common.services.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.web.bookingKol.auth.dtos.BrandRegisterRequestDTO;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private BrandRepository brandRepository;


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


    //     Phần đăng ký của người dùng
    @Override
    @Transactional
    public ResponseEntity<ApiResponse<?>> registerBrand(RegisterRequestDTO request)
            throws UserAlreadyExistsException {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email này đã được sử dụng");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setStatus(UserStatus.PENDING.name());
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        Role defaultRole = roleRepository.findByKey(Enums.Roles.USER.name())
                .orElseThrow(() -> new RuntimeException("Role USER không tìm thấy!"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(defaultRole);
        userRoleRepository.save(userRole);

        Brand brand = new Brand();
        brand.setUser(user);

        brand.setBrandName(
                (request.getBrandName() != null && !request.getBrandName().isBlank())
                        ? request.getBrandName()
                        : request.getFullName()
        );
        brandRepository.save(brand);

        String code = UUID.randomUUID().toString();
        EmailVerification ev = EmailVerification.builder()
                .user(user)
                .code(code)
                .expiredAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        emailVerificationRepository.save(ev);

        emailService.sendVerificationEmail(user.getEmail(), code);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message(List.of("Đăng ký thành công! Vui lòng kiểm tra email để xác thực."))
                        .build()
        );
    }




    @Override
    public ResponseEntity<ApiResponse<?>> verifyEmaildk(String email, String code) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        EmailVerification ev = emailVerificationRepository
                .findByUserAndCodeAndUsedFalse(user, code)
                .orElseThrow(() -> new IllegalArgumentException("Mã xác thực không hợp lệ hoặc đã dùng"));

        if (ev.getExpiredAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Mã xác thực đã hết hạn");
        }

        user.setStatus(UserStatus.ACTIVE.name());
        userRepository.save(user);
        ev.setUsed(true);
        emailVerificationRepository.save(ev);

        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Xác thực email thành công. Bạn có thể đăng nhập."))
                .build());
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
