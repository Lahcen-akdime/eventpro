package com.eventPro.api.Dto.Client;

import lombok.Builder;

@Builder
public record ClientUpdateRequest(
    String name ,
    String email ,
    String phone
)
{}
