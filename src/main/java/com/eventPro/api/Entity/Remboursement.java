package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutRemboursement;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Builder.Default;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Check;

@Getter
@Setter
@Table(
    name = "remboursements",
    uniqueConstraints = @UniqueConstraint(columnNames = "paiment_id")
)
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @Column(precision = 12, scale = 2,nullable = false)
    private BigDecimal amount ;

    @Column(length = 255)
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
        if(statut != StatutRemboursement.PENDING){
            throw new IllegalStateException("Seul un remboursement en attente peut etre approuve");
        }
        statut = StatutRemboursement.APPROVED ;
    }

    public void refuser(){
        if(statut != StatutRemboursement.PENDING){
            throw new IllegalStateException("Seul un remboursement en attente peut etre refuse");
        }
        statut = StatutRemboursement.REJECTED ;
    }

}
