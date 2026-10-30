package com.eventPro.api.Entity.Reservation;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "reservationLines")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    private Long quantite ;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice ;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id" , referencedColumnName = "id")
    private Reservation reservation ;

}
