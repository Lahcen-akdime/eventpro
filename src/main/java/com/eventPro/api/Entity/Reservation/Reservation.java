package com.eventPro.api.Entity.Reservation;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Promotion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String reference ;

    private LocalDateTime dateCreation = LocalDateTime.now() ;

    private LocalDateTime dateExpiration  ;

    private LocalDateTime startEvent ;

    private LocalDateTime endEvent ;

    @Enumerated(EnumType.STRING)
    private StatutReservation statutReservation ;
    private String title ;
    private String description ;
    private String place ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id" , referencedColumnName = "id")
    private Client client ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id",referencedColumnName = "id")
    private Promotion promotion ;

}
