package com.eventPro.api.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Table(name = "reservationLines")
@NoArgsConstructor
@Entity
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
