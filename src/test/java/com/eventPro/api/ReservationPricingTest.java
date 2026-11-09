package com.eventPro.api;

import com.eventPro.api.Entity.Enum.TypeReduction;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationPricingTest {

    @Test
    void calculatesPercentageDiscountAndReservationTotal() {
        Reservation reservation = Reservation.builder().build();
        reservation.addLine(LigneReservation.builder()
                .quantite(2)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("100.00"))
                .build());

        Promotion promotion = Promotion.builder()
                .typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("15"))
                .build();

        reservation.applyPromotion(promotion);
        reservation.calculateTotal();

        assertEquals(new BigDecimal("100.00"), reservation.getSubtotal());
        assertEquals(new BigDecimal("15.00"), reservation.getDiscountAmount());
        assertEquals(new BigDecimal("85.00"), reservation.getTotalAmount());
    }

    @Test
    void capsFixedDiscountAtSubtotal() {
        Promotion promotion = Promotion.builder()
                .typeReduction(TypeReduction.FIXED_AMOUNT)
                .valeurReduction(new BigDecimal("150.00"))
                .build();

        assertEquals(new BigDecimal("100.00"), promotion.calculateDiscount(new BigDecimal("100.00")));
    }
}
