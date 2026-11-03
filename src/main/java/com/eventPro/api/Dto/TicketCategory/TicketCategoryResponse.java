
package com.eventPro.api.Dto.TicketCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketCategoryResponse(
        String name,
        BigDecimal unitPrice,
        int totalQuantity,
        int MaxPerClient,
        int reservedPlaces,
        int ventedplaces,
        int availablePlaces
) {}
