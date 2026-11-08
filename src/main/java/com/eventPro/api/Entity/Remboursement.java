package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutRemboursement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder.Default;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Check;

@Getter
@Setter
@Table(name = "remboursements")
@Entity
@Builder 
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(precision = 12, scale = 2,nullable = false)
    private BigDecimal amount ;

    @Column(length = 255,nullable = false)
    private String motif ;

    @Column(nullable = false)
    private LocalDateTime requestedAt ;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutRemboursement statut = StatutRemboursement.PENDING ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiment_id" , referencedColumnName = "id",nullable = false)
    private Paiment paiment ;

    public void approuver(){
        if(statut.equals(StatutRemboursement.APPROVED)){
            throw new IllegalStateException("Le remboursement est approuve deja ");
        }
        statut = StatutRemboursement.APPROVED ;
    }

    public void refuser(){
        if(statut.equals(StatutRemboursement.REJECTED)){
            throw new IllegalStateException("Le remboursement est rejected deja ");
        }
        statut = StatutRemboursement.REJECTED ;
    }

}
