package com.web.bookingKol.domain.user.mappers;

import com.web.bookingKol.domain.user.dtos.UserDTO;
import com.web.bookingKol.domain.user.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDto(User user);
    List<UserDTO> toDtoList(List<User> users);
}
