package com.eventPro.api.Dto.Reservation;

import java.math.BigDecimal;
import java.util.UUID;

public record LigneReservationResponse(
        UUID categoryId,
        String categoryName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subTotal
) {}