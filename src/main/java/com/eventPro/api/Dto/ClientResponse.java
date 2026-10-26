package com.eventPro.api.Dto;

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
