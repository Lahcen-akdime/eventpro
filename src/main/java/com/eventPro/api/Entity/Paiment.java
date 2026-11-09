package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Reservation.Reservation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "paiments",indexes = {@Index(name = "reservation_id", columnList = "reservation_id")})
public class Paiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(unique = true,length = 64)
    @NotNull
    private String idempotencyKey ;

    @Column(unique = true)
    @NotNull
    private String reference ;

    @Column(precision = 12, scale = 2,nullable = false)
    @NotNull
    private BigDecimal amount ;

    @NotNull
    private LocalDateTime paidAt ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id" , referencedColumnName = "id",nullable = false)
    @NotNull
    private Reservation reservation ;


}
