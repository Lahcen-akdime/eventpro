package com.eventPro.api.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Table(name = "ticket_categories")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(nullable = false)
    @NotEmpty
    private String name ;

    @Column(nullable = false,precision = 12, scale = 2)
    private BigDecimal unitPrice ;

    @Column(nullable = false)
    private int totalQuantity ;

    @Column(nullable = false)
    private int quotaMaxParClient ;


    private int reservedPlaces = 0 ;

    private int ventedplaces = 0 ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",referencedColumnName = "id",nullable = false)
    private Event event ;

    public int getAvailblePlaces(){
        return totalQuantity - (reservedPlaces + ventedplaces) ;
    }


}
