package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.User.UserDto;
import com.eventPro.api.Dto.User.UserLoginRequest;
import com.eventPro.api.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toEntity(UserLoginRequest request);

    UserDto toDtoResponse(User User);
}
