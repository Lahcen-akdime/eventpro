package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TypeReduction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Column(unique = true)
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

    @Builder.Default
    Boolean active = true ;

    @NotNull
    @Builder.Default
    private Long nombreUtilisations = 0L ;

    private LocalDateTime createdAt = LocalDateTime.now() ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id" , referencedColumnName = "id")
    private Event event ;

    public void addUtilisation(Long numberUtilisation){
        nombreUtilisations += numberUtilisation ;
    }


    public boolean isValidAt(LocalDateTime now) {
        return Boolean.TRUE.equals(active)
                && !now.isBefore(startDate)
                && !now.isAfter(endDate)
                && nombreUtilisations < limiteUtilisation;
    }

    public boolean isApplicableTo(Event target) {
        return event == null || event.getId().equals(target.getId());
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        BigDecimal discount = (typeReduction == TypeReduction.PERCENTAGE)
                ? subtotal.multiply(valeurReduction).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : valeurReduction;
        return discount.min(subtotal).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

}
