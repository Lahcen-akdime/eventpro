package com.eventPro.api.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "TicketCategorys")
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    @NotBlank
    @NotEmpty
    private String name ;

    @NotBlank
    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal priceUnitaire ;

    @NotBlank
    @NotNull
    private Long quantiteTotal ;

    @NotBlank
    @NotNull
    private Long quotaMaxParClient ;

    private int placesReserved = 0 ;

    private int placesVented = 0 ;

    @JoinColumn(name = "event_id" , referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event ;


}
