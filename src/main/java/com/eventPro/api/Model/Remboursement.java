package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.StatutRemboursement;
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
@Table(name = "remboursements")
@Entity
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    @NotBlank
    @Column(precision = 12, scale = 2)
    private BigDecimal montant ;

    @NotNull
    @NotBlank
    private String motif ;

    @NotNull
    @NotBlank
    private LocalDateTime dateDemande ;

    @NotNull
    @NotBlank
    @Enumerated(EnumType.STRING)
    private StatutRemboursement state ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiment_id" , referencedColumnName = "id")
    private Paiment paiment ;

}
