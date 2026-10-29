
package com.eventPro.api.Dto.Ticket;

import com.eventPro.api.Entity.Enum.TicketStatus;

import java.util.UUID;

public record TicketVerificationResponse(
        UUID ticketId,
        String code,
        TicketStatus ticketState,
        boolean valid,
        String message
) {}
