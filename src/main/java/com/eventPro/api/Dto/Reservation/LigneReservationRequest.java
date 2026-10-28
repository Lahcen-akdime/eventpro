package com.eventPro.api.Dto.Reservation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record LigneReservationRequest(

        @NotNull
        UUID categoryId,

        @NotNull
        @Min(1)
        @Max(100)
        Integer quantity

) {}