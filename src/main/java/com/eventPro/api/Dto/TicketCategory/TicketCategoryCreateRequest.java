
package com.eventPro.api.Dto.TicketCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TicketCategoryCreateRequest(

        @NotBlank
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal unitPrice,

        @Positive
        int totalQuantity,

        @Positive
        int quotaMaxParClient

) {}
