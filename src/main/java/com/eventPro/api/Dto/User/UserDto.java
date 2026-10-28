package com.eventPro.api.Dto.User;

import com.eventPro.api.Entity.Enum.Role;
import lombok.Builder;

@Builder
public record UserDto(
        String name ,
        String email ,
        Role role
) {}
