package com.eventPro.api.Dto.Reservation;

import com.eventPro.api.Entity.Enum.StatutReservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReservationConclusionResponse(
    UUID id,
    String reference,
    StatutReservation status,
    UUID eventId,
    String eventTitle,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant expiresAt
){}
