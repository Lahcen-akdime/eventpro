package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.TicketAlreadyUsedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void checkMarksValidTicketAsUsedAtProvidedTime() {
        LocalDateTime checkedAt = LocalDateTime.of(2026, 11, 14, 12, 0);
        Ticket ticket = Ticket.builder().status(TicketStatus.VALID).build();

        ticket.check(checkedAt);

        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertEquals(checkedAt, ticket.getCheckedAt());
    }

    @Test
    void checkRejectsUsedAndCancelledTickets() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(TicketAlreadyUsedException.class,
                () -> Ticket.builder().status(TicketStatus.USED).build().check(now));
        assertThrows(InvalidOperationException.class,
                () -> Ticket.builder().status(TicketStatus.CANCELLED).build().check(now));
    }

    @Test
    void cancelMarksValidTicketCancelledAndIsIdempotent() {
        Ticket ticket = Ticket.builder().status(TicketStatus.VALID).build();

        ticket.cancel();
        ticket.cancel();

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
    }

    @Test
    void cancelRejectsUsedTicket() {
        Ticket ticket = Ticket.builder().status(TicketStatus.USED).build();

        assertThrows(TicketAlreadyUsedException.class, ticket::cancel);
    }
}
