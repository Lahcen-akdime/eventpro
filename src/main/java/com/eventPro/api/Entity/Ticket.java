package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table(name = "tickets",indexes = {
                                    @Index(columnList = "reservation_line_id"),
                                    @Index(columnList = "ticket_category_id")
})
@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(unique = false)
    private String code ;

    private LocalDateTime dateEmission ;

    private LocalDateTime dateControle  ;

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketState ;

    @JoinColumn(name = "ticket_category_id" , referencedColumnName = "id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TicketCategory ticketCategory ;

    @JoinColumn(name = "reservation_line_id" , referencedColumnName = "id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private LigneReservation ligneReservation ;
}
