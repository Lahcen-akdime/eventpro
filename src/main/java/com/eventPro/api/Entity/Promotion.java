package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TypeReduction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "promotions")
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
    private LocalDateTime start_date ;

    @NotNull
    private LocalDateTime end_date ;

    @NotNull
    private Long limiteUtilisation ;

    @NotNull
    private Long nombreUtilisations ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , referencedColumnName = "id")
    private Event event ;




}
