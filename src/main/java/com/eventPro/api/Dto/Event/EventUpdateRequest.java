package com.eventPro.api.Dto.Event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventUpdateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 255)
        String place,

        @NotNull
        LocalDateTime startDate,

        @NotNull
        LocalDateTime endDate,

        @NotNull
        LocalDateTime openDate,

        @NotNull
        LocalDateTime closeDate
) {}