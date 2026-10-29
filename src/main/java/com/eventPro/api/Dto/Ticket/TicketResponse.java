
package com.eventPro.api.Dto.Ticket;

import com.eventPro.api.Entity.Enum.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String code,
        LocalDateTime dateEmission,
        LocalDateTime dateControle,
        TicketStatus ticketState,
        UUID ticketCategoryId,
        String ticketCategoryName,
        UUID reservationLineId
) {}
