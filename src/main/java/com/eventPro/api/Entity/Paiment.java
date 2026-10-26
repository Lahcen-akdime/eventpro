package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutPaiement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "paiments")
@Entity
public class Paiment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(unique = true)
    @NotNull
    private String key ;

    @Column(precision = 12, scale = 2)
    @NotNull
    private BigDecimal montant ;

    @NotNull
    private LocalDateTime paymentDate ;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatutPaiement paymentStates ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id" , referencedColumnName = "id")
    @NotNull
    private Reservation reservation ;



}
