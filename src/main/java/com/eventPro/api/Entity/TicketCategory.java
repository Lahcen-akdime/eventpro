package com.eventPro.api.Entity;

import com.eventPro.api.Exception.InsufficientStockException;
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
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int maxPerClient;

    @Builder.Default
    @Column(nullable = false)
    private int reservedPlaces = 0;

    @Builder.Default
    @Column(nullable = false)
    private int soldPlaces = 0;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public int getAvailablePlaces(){
        return totalQuantity - (reservedPlaces + soldPlaces) ;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("La quantite doit etre positive");
        if (quantity > getAvailablePlaces()) throw new InsufficientStockException("Pas de places disponibles");
        reservedPlaces += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0 || quantity > reservedPlaces) throw new IllegalStateException("la quantite peut pas affecter");
        reservedPlaces -= quantity;
    }

    public void refund(int quantite){
        soldPlaces -= quantite ;
    }

}
