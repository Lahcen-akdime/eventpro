package com.eventPro.api.Entity.Reservation;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ReservationExpiredException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "reservations")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String reference ;

    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    private StatutReservation statutReservation;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id" , referencedColumnName = "id")
    private Client client ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id",referencedColumnName = "id")
    private Promotion promotion ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",referencedColumnName = "id")
    private Event event ;

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneReservation> lines = new ArrayList<>();



    public void addLine(LigneReservation line) {
        lines.add(line);
        line.setReservation(this); 
    }

    public void applyPromotion(Promotion promo) {
        this.promotion = promo;
        this.discountAmount = promo.calculateDiscount(computeSubtotal());
    }

    public void calculateTotal() {
        this.subtotal = computeSubtotal();
        this.totalAmount = subtotal.subtract(discountAmount)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSubtotal() {
        return lines.stream()
                .map(LigneReservation::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isExpiredAt(LocalDateTime now) {
        return dateExpiration != null && !now.isBefore(dateExpiration);
    }

    public void expire() {
        if (statutReservation != StatutReservation.PENDING_PAYMENT) {
            return;
        }
        statutReservation = StatutReservation.EXPIRED;
    }

    public void refund() {
        if (statutReservation != StatutReservation.CONFIRMED) {
            throw new IllegalStateException("Reservation must be confirmed before refund");
        }
        statutReservation = StatutReservation.REFUNDED;
    }

    public void cancel() {
        if (statutReservation != StatutReservation.PENDING_PAYMENT) {
            throw new InvalidOperationException("Seule une reservation en attente peut etre annulee");
        }
        statutReservation = StatutReservation.CANCELLED;
    }

    public void confirm(LocalDateTime now) {
        if (statutReservation != StatutReservation.PENDING_PAYMENT) {
            throw new InvalidOperationException("La reservation doit etre en attente de paiement");
        }
        if (isExpiredAt(now)) {
            throw new ReservationExpiredException("La reservation a expire");
        }
        statutReservation = StatutReservation.CONFIRMED;
    }

}
