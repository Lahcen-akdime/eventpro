
package com.eventPro.api.Dto.Ticket;

import com.eventPro.api.Entity.Enum.TicketStatus;

import java.util.UUID;

public record TicketResponse(
        UUID id,
        String code,
        TicketStatus status,
        String categoryName,
        String eventTitle
) {}
