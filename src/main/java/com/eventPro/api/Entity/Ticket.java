package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.TicketAlreadyUsedException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table(name = "tickets",indexes = {@Index(columnList = "reservation_line_id"), @Index(columnList = "ticket_category_id")})
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(unique = true)
    private String code ;

    private LocalDateTime issuedAt ;

    private LocalDateTime checkedAt  ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.VALID ;

    @JoinColumn(name = "ticket_category_id" , referencedColumnName = "id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TicketCategory ticketCategory ;

    @JoinColumn(name = "reservation_line_id" , referencedColumnName = "id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private LigneReservation reservationLine ;

    public void check(LocalDateTime now){
        if(status.equals(TicketStatus.USED)){
            throw new TicketAlreadyUsedException("Ticket already used") ;
        }
        if (status != TicketStatus.VALID){
            throw new InvalidOperationException("Ticket invalide") ;
        }
        this.status = TicketStatus.USED ;
        this.checkedAt = now ;
    }

    public void cancel(){
        if(status.equals(TicketStatus.CANCELLED)){
            return ;
        } 
        if(status.equals(TicketStatus.USED)){
            throw new TicketAlreadyUsedException("Le ticket est deja utilisé") ;
        }
        status = TicketStatus.CANCELLED ;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id",referencedColumnName = "id")
    private Client client ;

}
