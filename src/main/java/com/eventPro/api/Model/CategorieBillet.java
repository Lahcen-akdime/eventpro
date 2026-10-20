package com.eventPro.api.Model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CategorieBillet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    @NotBlank
    @NotEmpty
    private String name ;

    @NotBlank
    @NotNull
    private BigDecimal priceUnitaire ;

    @NotBlank
    @NotNull
    private Long quantiteTotal ;

    @NotBlank
    @NotNull
    private Long quotaMaxParClient ;

    private int placesReserved = 0 ;

    private int placesVented = 0 ;

}
