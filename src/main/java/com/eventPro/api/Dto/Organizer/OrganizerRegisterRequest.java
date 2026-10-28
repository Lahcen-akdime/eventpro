package com.eventPro.api.Dto.Organizer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OrganizerRegisterRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(max = 150)
        String organizationName,

        @NotBlank
        @Size(min = 6, max = 100)
        String password
        
) {}