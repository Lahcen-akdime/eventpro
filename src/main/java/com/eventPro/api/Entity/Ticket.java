package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TicketState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "tickets")
@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(unique = true)
    private String code ;

    private LocalDateTime dateEmission ;

    private LocalDateTime dateControle  ;

    @Enumerated(EnumType.STRING)
    private TicketState state ;

    @JoinColumn(name = "category_id" , referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TicketCategory ticketCategory ;

    @JoinColumn(name = "ligneReservation_id" , referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LigneReservation ligneReservation ;



}
