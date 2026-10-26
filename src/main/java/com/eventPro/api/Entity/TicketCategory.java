package com.eventPro.api.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "TicketCategorys")
@Entity
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    @NotEmpty
    private String name ;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal priceUnitaire ;

    @NotNull
    private Long quantiteTotal ;

    @NotNull
    private Long quotaMaxParClient ;

    private int placesReserved = 0 ;

    private int placesVented = 0 ;

    @ManyToOne
    @JoinColumn(name = "event_id",referencedColumnName = "id")
    private Event event ;

    public Long getPlacesDisponibles(){
        return quantiteTotal - (placesReserved + placesVented) ;
    }


}
