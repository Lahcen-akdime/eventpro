package com.eventPro.api.Dto.Reservation;

import com.eventPro.api.Entity.Enum.StatutReservation;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ClientReservationSearchRequest(
        List<StatutReservation> status,

        UUID eventId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdTo
) {}