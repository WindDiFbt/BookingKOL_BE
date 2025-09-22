package com.web.bookingKol.domain.user.services.impl;

import com.web.bookingKol.common.payload.ApiResponse;
import com.web.bookingKol.domain.user.dtos.UserDTO;
import com.web.bookingKol.domain.user.mappers.UserMapper;
import com.web.bookingKol.domain.user.models.User;
import com.web.bookingKol.domain.user.repositories.UserRepository;
import com.web.bookingKol.domain.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    @Override
    public ApiResponse<List<UserDTO>> getAllUser() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = userMapper.toDtoList(users);
        return ApiResponse.<List<UserDTO>>builder()
                .status(HttpStatus.OK.value())
                .message(List.of("Get all users success"))
                .data(userDTOs)
                .build();
    }
}
