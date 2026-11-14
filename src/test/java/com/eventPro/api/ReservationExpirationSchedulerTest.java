package com.eventPro.api;

import com.eventPro.api.Scheduler.ReservationExpirationScheduler;
import com.eventPro.api.Service.ReservationExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
class ReservationExpirationSchedulerTest {

    @Mock
    private ReservationExpirationService reservationExpirationService;

    @InjectMocks
    private ReservationExpirationScheduler scheduler;

    @Test
    void expireOverdueReservations_delegueAuServiceUneSeuleFois() {
        // ACT
        scheduler.expireOverdueReservations();

        // ASSERT
        verify(reservationExpirationService, times(1)).expireOverdueReservations();
        verifyNoMoreInteractions(reservationExpirationService);
    }

    @Test
    void expireOverdueReservations_laTacheEstPlanifieeAvecLIntervalleConfigurable() throws NoSuchMethodException {
        // le scheduler doit etre annote @Scheduled avec la propriete (60000 ms par defaut)
        Scheduled scheduled = ReservationExpirationScheduler.class
                .getMethod("expireOverdueReservations")
                .getAnnotation(Scheduled.class);

        assertNotNull(scheduled);
        assertEquals("${eventpro.reservation.expiration-scan-interval:60000}", scheduled.fixedDelayString());
    }
}