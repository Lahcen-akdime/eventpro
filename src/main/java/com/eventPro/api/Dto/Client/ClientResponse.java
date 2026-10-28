package com.eventPro.api.Dto.Client;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClientResponse(
    UUID id ,
    String name ,
    String email ,
    String phone
)
{}
