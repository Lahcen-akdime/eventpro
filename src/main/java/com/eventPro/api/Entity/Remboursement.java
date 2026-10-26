package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutRemboursement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Table(name = "remboursements")
@Entity
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    @Column(precision = 12, scale = 2)
    private BigDecimal montant ;

    @NotNull
    private String motif ;

    @NotNull
    private LocalDateTime dateDemande ;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatutRemboursement state ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiment_id" , referencedColumnName = "id")
    private Paiment paiment ;

}
