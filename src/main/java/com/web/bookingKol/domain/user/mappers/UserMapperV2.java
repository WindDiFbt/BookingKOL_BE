package com.web.bookingKol.domain.user.mappers;

import com.web.bookingKol.domain.user.dtos.UserDetailDTO;
import com.web.bookingKol.domain.user.models.Role;
import com.web.bookingKol.domain.user.models.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapperV2 {
    public UserDetailDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setFullName(user.getFullName());
        dto.setGender(user.getGender());
        dto.setAddress(user.getAddress());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setTimezone(user.getTimezone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        if (user.getStatus() != null) {
            dto.setStatus(user.getStatus());
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getKey)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        } else {
            dto.setRoles(Collections.emptySet());
        }
        return dto;
    }

}
