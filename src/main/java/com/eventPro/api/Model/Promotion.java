package com.eventPro.api.Model;

import com.eventPro.api.Model.Enum.TypeReduction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String code ;

    @NotNull
    @NotBlank
    @Enumerated(EnumType.STRING)
    private TypeReduction typeReduction ;

    @NotNull
    @NotBlank
    private BigDecimal valeurReduction ;

    @NotNull
    @NotBlank
    private LocalDateTime start ;

    @NotNull
    @NotBlank
    private LocalDateTime end ;

    @NotNull
    @NotBlank
    private Long limiteUtilisation ;

    @NotNull
    @NotBlank
    private Long nombreUtilisations ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , referencedColumnName = "id")
    private Event event ;




}
