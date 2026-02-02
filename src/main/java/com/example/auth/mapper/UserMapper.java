package com.example.auth.mapper;

import com.example.auth.dto.UserDto;
import com.example.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "password", ignore = true)
    @org.mapstruct.Mapping(target = "role", constant = "USER")
    @org.mapstruct.Mapping(target = "enabled", constant = "true")
    @org.mapstruct.Mapping(target = "createdAt", ignore = true)
    User toUser(com.example.auth.dto.RegisterRequest request);
}
