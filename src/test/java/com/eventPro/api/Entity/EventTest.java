package com.eventPro.api.Entity;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private final LocalDateTime openDate = LocalDateTime.of(2026, 11, 1, 9, 0);
    private final LocalDateTime closeDate = LocalDateTime.of(2026, 11, 30, 18, 0);
    private final LocalDateTime endDate = LocalDateTime.of(2026, 12, 1, 9, 0);

    @Test
    void publishAndCancelUpdateEventState() {
        Event event = Event.builder().state(StatutEvenement.DRAFT).build();

        event.publier();
        assertEquals(StatutEvenement.PUBLISHED, event.getState());

        event.annuler();
        assertEquals(StatutEvenement.CANCELLED, event.getState());
    }
}
