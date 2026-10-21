package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.StatutPaiement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String key ;

    @Column(precision = 12, scale = 2)
    @NotNull
    @NotBlank
    private BigDecimal montant ;

    @NotNull
    @NotBlank
    private LocalDateTime paymentDate ;

    @Enumerated(EnumType.STRING)
    @NotNull
    @NotBlank
    private StatutPaiement paymentStates ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id" , referencedColumnName = "id")
    @NotNull
    @NotBlank
    private Reservation reservation ;



}
