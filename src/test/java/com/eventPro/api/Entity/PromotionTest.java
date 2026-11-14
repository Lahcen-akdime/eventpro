package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TypeReduction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {

    private final LocalDateTime start = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 11, 30, 23, 59);

    @Test
    void addUtilisationAddsRequestedNumber() {
        Promotion promotion = Promotion.builder().nombreUtilisations(2L).build();

        promotion.addUtilisation(3L);

        assertEquals(5L, promotion.getNombreUtilisations());
    }

    @Test
    void isValidAtIncludesStartAndEndAndRequiresActiveUnusedCapacity() {
        Promotion promotion = validPromotion();

        assertTrue(promotion.isValidAt(start));
        assertTrue(promotion.isValidAt(end));
        assertFalse(promotion.isValidAt(start.minusNanos(1)));
        assertFalse(promotion.isValidAt(end.plusNanos(1)));
        assertFalse(Promotion.builder().active(false).startDate(start).endDate(end)
                .nombreUtilisations(0L).limiteUtilisation(1L).build().isValidAt(start));
        assertFalse(Promotion.builder().active(null).startDate(start).endDate(end)
                .nombreUtilisations(0L).limiteUtilisation(1L).build().isValidAt(start));
        assertFalse(Promotion.builder().active(true).startDate(start).endDate(end)
                .nombreUtilisations(1L).limiteUtilisation(1L).build().isValidAt(start));
    }

    @Test
    void isApplicableToAcceptsGlobalAndMatchingEventPromotionsOnly() {
        Event target = Event.builder().id(UUID.randomUUID()).build();
        Event matching = Event.builder().id(target.getId()).build();
        Event other = Event.builder().id(UUID.randomUUID()).build();

        assertTrue(Promotion.builder().build().isApplicableTo(target));
        assertTrue(Promotion.builder().event(matching).build().isApplicableTo(target));
        assertFalse(Promotion.builder().event(other).build().isApplicableTo(target));
    }

    @Test
    void calculateDiscountRoundsPercentageAndCapsAtSubtotal() {
        Promotion percentage = Promotion.builder().typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("12.5")).build();

        assertEquals(new BigDecimal("12.50"), percentage.calculateDiscount(new BigDecimal("100.00")));
        assertEquals(new BigDecimal("0.01"),
                percentage.calculateDiscount(new BigDecimal("0.05")));
        assertEquals(new BigDecimal("10.00"),
                Promotion.builder().typeReduction(TypeReduction.PERCENTAGE)
                        .valeurReduction(new BigDecimal("150")).build()
                        .calculateDiscount(new BigDecimal("10.00")));
    }

    @Test
    void calculateDiscountClampsFixedAndNegativeDiscountsToValidRange() {
        assertEquals(new BigDecimal("10.00"),
                Promotion.builder().typeReduction(TypeReduction.FIXED_AMOUNT)
                        .valeurReduction(new BigDecimal("15.00")).build()
                        .calculateDiscount(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("0.00"),
                Promotion.builder().typeReduction(TypeReduction.FIXED_AMOUNT)
                        .valeurReduction(new BigDecimal("-5.00")).build()
                        .calculateDiscount(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("0.00"),
                Promotion.builder().typeReduction(TypeReduction.FIXED_AMOUNT)
                        .valeurReduction(new BigDecimal("5.00")).build()
                        .calculateDiscount(new BigDecimal("-1.00")));
    }

    private Promotion validPromotion() {
        return Promotion.builder().active(true).startDate(start).endDate(end)
                .nombreUtilisations(0L).limiteUtilisation(1L).build();
    }
}
