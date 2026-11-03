package com.eventPro.api.Dto.Event;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EventCreateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        String description,

        @NotBlank
        @Size(max = 255)
        String place,

        int capacite ,

        @NotNull
        LocalDateTime startDate,

        @NotNull
        LocalDateTime endDate,

        @NotNull
        LocalDateTime openDate,

        @NotNull
        LocalDateTime closeDate ,

        @NotEmpty
        @Size(max = 10)
        List<@Valid TicketCategoryCreateRequest> categories
) {}