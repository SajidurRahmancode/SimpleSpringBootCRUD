package com.simple.crud.demo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.simple.crud.demo.model.dto.UserCreateDto;
import com.simple.crud.demo.model.dto.UserResponseDto;
import com.simple.crud.demo.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User entity);
    User toEntity(UserCreateDto dto);
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UserCreateDto dto, @MappingTarget User entity);
}
