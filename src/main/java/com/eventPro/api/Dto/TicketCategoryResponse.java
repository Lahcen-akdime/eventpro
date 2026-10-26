package com.eventPro.api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public class TicketCategoryResponse {

    private UUID id ;

    @NotNull
    @NotBlank
    private String name ;

    private BigDecimal priceUnitaire ;

    private Long quantiteTotal ;

    private Long quotaMaxParClient ;

    private String disponiblePlaces ;
}
