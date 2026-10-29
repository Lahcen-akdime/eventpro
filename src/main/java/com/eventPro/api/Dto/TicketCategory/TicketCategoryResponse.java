
package com.eventPro.api.Dto.TicketCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketCategoryResponse(
        UUID id,
        String name,
        BigDecimal unitPrice,
        int totalQuantity,
        int quotaMaxParClient,
        int reservedPlaces,
        int ventedplaces,
        int availablePlaces,
        UUID eventId
) {}
