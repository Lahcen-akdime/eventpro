package com.eventPro.api.Dto.Reservation;

import com.eventPro.api.Entity.Enum.StatutReservation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminReservationSearchRequest(
        List<StatutReservation> status,

        UUID eventId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdTo,

        UUID clientId,

        @Size(max = 20)
        String reference,

        @DecimalMin("0.00")
        BigDecimal minTotal,

        @DecimalMin("0.00")
        BigDecimal maxTotal,

        Boolean promoUsed
) {}