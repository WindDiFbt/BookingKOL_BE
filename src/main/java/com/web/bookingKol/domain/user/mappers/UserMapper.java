package com.web.bookingKol.domain.user.mappers;

import com.web.bookingKol.domain.user.dtos.UserDTO;
import com.web.bookingKol.domain.user.models.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);

    List<UserDTO> toDtoList(List<User> users);
}
