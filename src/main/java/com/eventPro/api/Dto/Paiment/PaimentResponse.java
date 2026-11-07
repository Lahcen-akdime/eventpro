package com.eventPro.api.Dto.Paiment;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Enum.StatutPaiement;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaimentResponse(
        UUID id,
        String reference,
        UUID reservationId,
        BigDecimal amount,
        StatutPaiement statut,
        LocalDateTime paidAt,
        List<TicketResponse> tickets
) {}
