package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.TicketState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "tickets")
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
