package com.eventPro.api.Dto.Reservation;

import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String reference,
        StatutReservation status,
        UUID eventId,
        String eventTitle,
        Instant createdAt,
        Instant expiresAt,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String promoCode,
        List<LigneReservation> lines
) {}