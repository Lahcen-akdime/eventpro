package com.eventPro.api.Entity.Reservation;

import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Enum.TypeReduction;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ReservationExpiredException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void addLineAddsLineAndSetsItsReservation() {
        Reservation reservation = Reservation.builder().build();
        LigneReservation line = LigneReservation.builder().build();

        reservation.addLine(line);

        assertSame(line, reservation.getLines().get(0));
        assertSame(reservation, line.getReservation());
    }

    @Test
    void applyPromotionAndCalculateTotalSetSubtotalDiscountAndTotal() {
        Reservation reservation = reservationWithSubtotal("100.00");
        Promotion promotion = Promotion.builder().typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("20")).build();

        reservation.applyPromotion(promotion);
        reservation.calculateTotal();

        assertSame(promotion, reservation.getPromotion());
        assertEquals(new BigDecimal("100.00"), reservation.getSubtotal());
        assertEquals(new BigDecimal("20.00"), reservation.getDiscountAmount());
        assertEquals(new BigDecimal("80.00"), reservation.getTotalAmount());
    }

    @Test
    void calculateTotalNeverReturnsNegativeTotal() {
        Reservation reservation = reservationWithSubtotal("10.00");
        reservation.setDiscountAmount(new BigDecimal("15.00"));

        reservation.calculateTotal();

        assertEquals(new BigDecimal("0.00"), reservation.getTotalAmount());
    }

    @Test
    void isExpiredAtHandlesMissingFutureAndReachedExpiration() {
        LocalDateTime expiration = LocalDateTime.of(2026, 11, 14, 12, 0);
        Reservation withoutExpiration = Reservation.builder().build();
        Reservation reservation = Reservation.builder().dateExpiration(expiration).build();

        assertFalse(withoutExpiration.isExpiredAt(expiration));
        assertFalse(reservation.isExpiredAt(expiration.minusNanos(1)));
        assertTrue(reservation.isExpiredAt(expiration));
        assertTrue(reservation.isExpiredAt(expiration.plusNanos(1)));
    }

    @Test
    void expireOnlyChangesPendingPaymentReservations() {
        Reservation pending = Reservation.builder()
                .statutReservation(StatutReservation.PENDING_PAYMENT).build();
        Reservation confirmed = Reservation.builder()
                .statutReservation(StatutReservation.CONFIRMED).build();

        pending.expire();
        confirmed.expire();

        assertEquals(StatutReservation.EXPIRED, pending.getStatutReservation());
        assertEquals(StatutReservation.CONFIRMED, confirmed.getStatutReservation());
    }

    @Test
    void refundRequiresConfirmedReservation() {
        Reservation confirmed = Reservation.builder()
                .statutReservation(StatutReservation.CONFIRMED).build();
        Reservation pending = Reservation.builder()
                .statutReservation(StatutReservation.PENDING_PAYMENT).build();

        confirmed.refund();

        assertEquals(StatutReservation.REFUNDED, confirmed.getStatutReservation());
        assertThrows(IllegalStateException.class, pending::refund);
    }

    @Test
    void cancelRequiresPendingPaymentReservation() {
        Reservation pending = Reservation.builder()
                .statutReservation(StatutReservation.PENDING_PAYMENT).build();
        Reservation confirmed = Reservation.builder()
                .statutReservation(StatutReservation.CONFIRMED).build();

        pending.cancel();

        assertEquals(StatutReservation.CANCELLED, pending.getStatutReservation());
        assertThrows(InvalidOperationException.class, confirmed::cancel);
    }

    @Test
    void confirmRequiresPendingAndUnexpiredReservation() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 14, 12, 0);
        Reservation pending = Reservation.builder()
                .statutReservation(StatutReservation.PENDING_PAYMENT)
                .dateExpiration(now.plusMinutes(1)).build();
        Reservation expired = Reservation.builder()
                .statutReservation(StatutReservation.PENDING_PAYMENT)
                .dateExpiration(now).build();
        Reservation confirmed = Reservation.builder()
                .statutReservation(StatutReservation.CONFIRMED).build();

        pending.confirm(now);

        assertEquals(StatutReservation.CONFIRMED, pending.getStatutReservation());
        assertThrows(ReservationExpiredException.class, () -> expired.confirm(now));
        assertThrows(InvalidOperationException.class, () -> confirmed.confirm(now));
    }

    private Reservation reservationWithSubtotal(String subtotal) {
        Reservation reservation = Reservation.builder().build();
        reservation.addLine(LigneReservation.builder().subtotal(new BigDecimal(subtotal)).build());
        return reservation;
    }
}
