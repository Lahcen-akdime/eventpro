package com.eventPro.api.Dto.Reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReservationCreateRequest(

        @NotNull
        UUID eventId,

        @NotEmpty
        @Size(max = 10)
        List<@Valid LigneReservationRequest> lines,

        @Pattern(regexp = "^[A-Za-z0-9_-]{4,20}$")
        String promoCode

) {}