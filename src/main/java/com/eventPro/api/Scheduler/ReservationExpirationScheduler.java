package com.eventPro.api.Scheduler;

import com.eventPro.api.Service.ReservationExpirationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpirationScheduler {

    private final ReservationExpirationService reservationExpirationService;

    @Scheduled(fixedDelayString = "${eventpro.reservation.expiration-scan-interval:60000}")
    public void expireOverdueReservations() {
        reservationExpirationService.expireOverdueReservations();
    }
    
}
