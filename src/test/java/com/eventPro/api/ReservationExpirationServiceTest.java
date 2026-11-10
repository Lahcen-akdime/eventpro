package com.eventPro.api;

import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.ReservationExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 12, 0);

    @Mock
    private ReservationJpaRepository reservationJpaRepository;

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationExpirationService reservationExpirationService;

    @Test
    void doesNotExpireReservationThatIsNotDue() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .statutReservation(StatutReservation.PENDING_PAYMENT)
                .dateExpiration(NOW.plusMinutes(1))
                .build();

        when(clock.instant()).thenReturn(NOW.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(reservationJpaRepository.findIdsToExpire(
                eq(StatutReservation.PENDING_PAYMENT), eq(NOW), any()))
                .thenReturn(List.of(reservationId));
        when(reservationJpaRepository.findByIdForUpdate(reservationId))
                .thenReturn(Optional.of(reservation));

        assertEquals(0, reservationExpirationService.expireOverdueReservations());
        assertEquals(StatutReservation.PENDING_PAYMENT, reservation.getStatutReservation());
    }
}
