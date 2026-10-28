package com.eventPro.api.Dto.User;

import lombok.Builder;

@Builder
public record UserLoginRequest(
     String email ,
     String password
){}
