package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TypeReduction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "promotions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id ;

    @NotNull
    private String code ;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TypeReduction typeReduction ;

    @NotNull
    private BigDecimal valeurReduction ;

    @NotNull
    private LocalDateTime startDate ;

    @NotNull
    private LocalDateTime endDate ;

    @NotNull
    private Long limiteUtilisation ;

    @NotNull
    @Builder.Default
    private Long nombreUtilisations = 0L ;

    private LocalDateTime createdAt = LocalDateTime.now() ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , referencedColumnName = "id")
    private Event event ;

}
