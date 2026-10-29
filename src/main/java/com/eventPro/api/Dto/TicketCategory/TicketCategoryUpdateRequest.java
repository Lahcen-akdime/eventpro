
package com.eventPro.api.Dto.TicketCategory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TicketCategoryUpdateRequest(

        @NotBlank
        String name,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal unitPrice,

        @Min(1)
        int totalQuantity,

        @Min(1)
        int quotaMaxParClient

) {}
