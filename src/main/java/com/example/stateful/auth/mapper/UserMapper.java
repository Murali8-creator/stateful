package com.example.stateful.auth.mapper;

import com.example.stateful.auth.dto.response.UserDTO;
import com.example.stateful.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDTO toDTO(User user);
}
