package com.eventPro.api;

import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Remboursement;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Mapper.RemboursementMapper;
import com.eventPro.api.Repository.PaimentJpaRepository;
import com.eventPro.api.Repository.RemboursementJpaRepository;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.RemboursementService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemboursementServiceTest {

    @Mock
    private RemboursementJpaRepository remboursementJpaRepository;
    @Mock
    private RemboursementMapper remboursementMapper;
    @Mock
    private ReservationJpaRepository reservationJpaRepository;
    @Mock
    private PaimentJpaRepository paimentJpaRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private Clock clock;

    @InjectMocks
    private RemboursementService remboursementService;

    @Test
    void returnsExistingRefundWithoutCreatingADuplicate() {
        UUID clientId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .client(client)
                .statutReservation(StatutReservation.CONFIRMED)
                .build();
        Paiment payment = Paiment.builder()
                .id(paymentId)
                .statut(StatutPaiement.SUCCESS)
                .reservation(reservation)
                .build();
        Remboursement existingRefund = Remboursement.builder()
                .paiment(payment)
                .amount(new BigDecimal("25.00"))
                .build();
        RemboursementResponse response = new RemboursementResponse(
                UUID.randomUUID(), reservationId, "RES-1", new BigDecimal("25.00"),
                "duplicate request", null, existingRefund.getStatut());
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(clock.instant()).thenReturn(Instant.parse("2026-11-09T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(reservationJpaRepository.findByIdForUpdate(reservationId))
                .thenReturn(Optional.of(reservation));
        when(paimentJpaRepository.findByReservationIdAndStatut(reservationId, StatutPaiement.SUCCESS))
                .thenReturn(Optional.of(payment));
        when(remboursementJpaRepository.findByPaimentId(paymentId))
                .thenReturn(Optional.of(existingRefund));
        when(remboursementMapper.toResponse(existingRefund)).thenReturn(response);

        assertSame(response, remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("duplicate request"),
                request));
        verify(remboursementJpaRepository, never()).save(any(Remboursement.class));
    }
}
