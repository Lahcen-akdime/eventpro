package com.eventPro.api.Dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class TicketCategoryRequest {

    @NotNull
    @NotBlank
    private String name ;

    private BigDecimal priceUnitaire ;

    private Long quantiteTotal ;

    private Long quotaMaxParClient ;

}
