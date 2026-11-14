package com.eventPro.api.Entity;

import com.eventPro.api.Exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketCategoryTest {

    @Test
    void availablePlacesSubtractsReservationsAndSales() {
        TicketCategory category = category(10, 2, 3);

        assertEquals(5, category.getAvailablePlaces());
    }

    @Test
    void reserveUpdatesReservedPlacesAndRejectsInvalidQuantities() {
        TicketCategory category = category(10, 2, 3);

        category.reserve(4);
        assertEquals(6, category.getReservedPlaces());
        assertEquals(1, category.getAvailablePlaces());
        assertThrows(IllegalArgumentException.class, () -> category.reserve(0));
        assertThrows(IllegalArgumentException.class, () -> category.reserve(-1));
        assertThrows(InsufficientStockException.class, () -> category.reserve(2));
    }

    @Test
    void releaseUpdatesReservationsAndRejectsInvalidQuantities() {
        TicketCategory category = category(10, 4, 2);

        category.release(3);

        assertEquals(1, category.getReservedPlaces());
        assertThrows(IllegalStateException.class, () -> category.release(0));
        assertThrows(IllegalStateException.class, () -> category.release(2));
    }

    @Test
    void confirmSaleMovesReservedPlacesToSoldPlaces() {
        TicketCategory category = category(10, 4, 2);

        category.confirmSale(3);

        assertEquals(1, category.getReservedPlaces());
        assertEquals(5, category.getSoldPlaces());
        assertEquals(4, category.getAvailablePlaces());
    }

    @Test
    void confirmSaleRejectsInvalidOrUnreservedQuantities() {
        TicketCategory category = category(10, 2, 0);

        assertThrows(IllegalArgumentException.class, () -> category.confirmSale(0));
        assertThrows(IllegalArgumentException.class, () -> category.confirmSale(-1));
        assertThrows(IllegalStateException.class, () -> category.confirmSale(3));
    }

    @Test
    void refundRestoresAvailabilityAndRejectsInvalidOrUnsoldQuantities() {
        TicketCategory category = category(10, 0, 4);

        category.refund(3);

        assertEquals(1, category.getSoldPlaces());
        assertEquals(9, category.getAvailablePlaces());
        assertThrows(IllegalArgumentException.class, () -> category.refund(0));
        assertThrows(IllegalArgumentException.class, () -> category.refund(-1));
        assertThrows(IllegalStateException.class, () -> category.refund(2));
    }

    private TicketCategory category(int total, int reserved, int sold) {
        return TicketCategory.builder().totalQuantity(total).reservedPlaces(reserved).soldPlaces(sold).build();
    }
}
