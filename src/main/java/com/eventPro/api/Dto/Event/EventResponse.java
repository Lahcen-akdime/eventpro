package com.eventPro.api.Dto.Event;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record EventResponse(
         UUID id,
         String title,
         String description,
         String place,
         int capacite ,
         LocalDateTime startDate,
         LocalDateTime endDate,
         LocalDateTime openDate,
         LocalDateTime closeDate,
         StatutEvenement state,
         String organizerId,
         String organizerName,
         String organizationName
) {}