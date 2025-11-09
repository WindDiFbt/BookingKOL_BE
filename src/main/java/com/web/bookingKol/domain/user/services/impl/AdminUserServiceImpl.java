package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.Enums;
import com.web.bookingKol.common.Enums.UserStatus;
import com.web.bookingKol.common.exception.UserAlreadyExistsException;
import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.AdminAccountDTO;
import com.web.bookingKol.domain.user.dtos.UserDetailDTO;
import com.web.bookingKol.domain.user.mappers.UserMapper;
import com.web.bookingKol.domain.user.mappers.UserMapperV2;
import com.web.bookingKol.domain.user.models.Role;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.RoleRepository;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapperV2 userMapperV2;
    @Autowired
    private UserMapper userMapper;

    @Override
    public ApiResponse<?> updateUserStatus(UUID userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (!status.equalsIgnoreCase(UserStatus.ACTIVE.name())
                && !status.equalsIgnoreCase(UserStatus.SUSPENDED.name())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(List.of("Trạng thái chỉ có thể là ACTIVE hoặc SUSPENDED"))
                    .build();
        }

        UserStatus newStatus = UserStatus.valueOf(status.toUpperCase());
        user.setStatus(newStatus.name());
        userRepository.save(user);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Cập nhật trạng thái người dùng thành công"))
                .data(user.getStatus())
                .build();
    }

    @Override
    public ApiResponse<?> createAdminAccount(AdminAccountDTO adminAccountDTO) {
        Role role = roleRepository.findByKey(Enums.Roles.ADMIN.name())
                .orElseThrow(() -> new IllegalArgumentException("Role ADMIN không tìm thấy !!"));
        User newAdmin = new User();
        if (userRepository.existsByEmail(adminAccountDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email đã tồn tại " + adminAccountDTO.getEmail());
        }
        newAdmin.setEmail(adminAccountDTO.getEmail());
        newAdmin.setPhone(adminAccountDTO.getPhone());
        newAdmin.setFullName(adminAccountDTO.getFullName());
        newAdmin.setPasswordHash(passwordEncoder.encode(adminAccountDTO.getPassword()));
        if (adminAccountDTO.getGender() != null) {
            newAdmin.setGender(adminAccountDTO.getGender().trim().equalsIgnoreCase(Enums.UserGender.Male.name()) ?
                    Enums.UserGender.Male.name() : Enums.UserGender.Female.name());
        }
        newAdmin.setAddress(adminAccountDTO.getAddress());
        newAdmin.setRoles(new LinkedHashSet<>(Collections.singletonList(role)));
        newAdmin.setAddress(adminAccountDTO.getAddress());
        newAdmin.setStatus(Enums.UserStatus.ACTIVE.name());
        newAdmin.setCreatedAt(Instant.now());
        userRepository.save(newAdmin);

        return ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Tạo tài khoản admin thành công!"))
                .data(userMapper.toDto(newAdmin))
                .build();
    }

    @Override
    public ApiResponse<Page<UserDetailDTO>> getAllAccountWithFilter(Pageable pageable, String status, String role) {
        Page<User> userPage = userRepository.findAllWithFilters(status, role, pageable);
        return ApiResponse.<Page<UserDetailDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Lấy danh sách người dùng thành công!"))
                .data(userPage.map(userMapperV2::toDto))
                .build();
    }
}

