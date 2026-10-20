package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.TypeReduction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Promotion {

    private UUID id ;
    private String code ;
    private TypeReduction typeReduction ;
    private BigDecimal valeurReduction ;
    private LocalDateTime start ;
    private LocalDateTime end ;
    private Long limiteUtilisation ;
    private int nombreUtilisations ;
}
