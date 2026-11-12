package com.eventPro.api;

import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Remboursement;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutRemboursement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.RemboursementMapper;
import com.eventPro.api.Repository.PaimentJpaRepository;
import com.eventPro.api.Repository.RemboursementJpaRepository;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.RemboursementService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setClock() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-11-09T12:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

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

    @Test
    void savesARejectedRefundForAConfirmedReservation() {
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
        HttpServletRequest request = mock(HttpServletRequest.class);
        RemboursementResponse response = new RemboursementResponse(
                UUID.randomUUID(), reservationId, "RES-2", BigDecimal.ZERO,
                "test reason", LocalDateTime.now(clock), StatutRemboursement.REJECTED);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        when(paimentJpaRepository.findByReservationIdAndStatut(reservationId, StatutPaiement.SUCCESS))
                .thenReturn(Optional.of(payment));
        when(remboursementJpaRepository.findByPaimentId(paymentId)).thenReturn(Optional.empty());
        when(remboursementJpaRepository.save(any(Remboursement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(remboursementMapper.toResponse(any(Remboursement.class))).thenReturn(response);

        assertSame(response, remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("test reason"), request));

        ArgumentCaptor<Remboursement> savedRefund = ArgumentCaptor.forClass(Remboursement.class);
        verify(remboursementJpaRepository).save(savedRefund.capture());
        assertEquals(BigDecimal.ZERO, savedRefund.getValue().getAmount());
        assertEquals("test reason", savedRefund.getValue().getMotif());
        assertEquals(LocalDateTime.of(2026, 11, 9, 12, 0), savedRefund.getValue().getRequestedAt());
        assertSame(payment, savedRefund.getValue().getPaiment());
        assertEquals(StatutRemboursement.REJECTED, savedRefund.getValue().getStatut());
    }

    @Test
    void rejectsRefundWhenReservationDoesNotExist() {
        UUID reservationId = UUID.randomUUID();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientService.getAuthaunticatedClient(request))
                .thenReturn(Client.builder().id(UUID.randomUUID()).build());
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("reason"), request));
        verifyNoInteractions(paimentJpaRepository);
    }

    @Test
    void rejectsRefundWhenReservationBelongsToAnotherClient() {
        UUID reservationId = UUID.randomUUID();
        Client authenticatedClient = Client.builder().id(UUID.randomUUID()).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .client(Client.builder().id(UUID.randomUUID()).build())
                .statutReservation(StatutReservation.CONFIRMED)
                .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(authenticatedClient);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidVDataException.class, () -> remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("reason"), request));
        verifyNoInteractions(paimentJpaRepository);
    }

    @Test
    void rejectsRefundWhenNoSuccessfulPaymentExists() {
        UUID reservationId = UUID.randomUUID();
        Client client = Client.builder().id(UUID.randomUUID()).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .client(client)
                .statutReservation(StatutReservation.CONFIRMED)
                .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        when(paimentJpaRepository.findByReservationIdAndStatut(reservationId, StatutPaiement.SUCCESS))
                .thenReturn(Optional.empty());

        assertThrows(InvalidOperationException.class, () -> remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("reason"), request));
        verifyNoInteractions(remboursementJpaRepository);
    }

    @Test
    void rejectsRefundWhenReservationIsNotConfirmed() {
        UUID reservationId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Client client = Client.builder().id(UUID.randomUUID()).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .client(client)
                .statutReservation(StatutReservation.CANCELLED)
                .build();
        Paiment payment = Paiment.builder().id(paymentId).reservation(reservation).build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        when(paimentJpaRepository.findByReservationIdAndStatut(reservationId, StatutPaiement.SUCCESS))
                .thenReturn(Optional.of(payment));
        when(remboursementJpaRepository.findByPaimentId(paymentId)).thenReturn(Optional.empty());

        assertThrows(InvalidOperationException.class, () -> remboursementService.refund(
                reservationId, new com.eventPro.api.Dto.Remboursement.RemboursementRequest("reason"), request));
        verify(remboursementJpaRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void findsRefundAndMapsItToResponse() {
        UUID refundId = UUID.randomUUID();
        Remboursement refund = Remboursement.builder().id(refundId).build();
        RemboursementResponse response = new RemboursementResponse(
                refundId, UUID.randomUUID(), "RES-3", BigDecimal.TEN, "reason",
                LocalDateTime.now(clock), StatutRemboursement.PENDING);
        when(remboursementJpaRepository.findById(refundId)).thenReturn(Optional.of(refund));
        when(remboursementMapper.toResponse(refund)).thenReturn(response);

        assertSame(refund, remboursementService.findById(refundId));
        assertSame(response, remboursementService.getById(refundId));
        verify(remboursementJpaRepository, times(2)).findById(refundId);
    }

    @Test
    void throwsWhenRefundCannotBeFound() {
        UUID refundId = UUID.randomUUID();
        when(remboursementJpaRepository.findById(refundId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> remboursementService.findById(refundId));
        assertThrows(ResourceNotFoundException.class, () -> remboursementService.getById(refundId));
    }

    @Test
    void listsRefundsForAuthenticatedClientAndForAdmin() {
        UUID clientId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        Pageable pageable = PageRequest.of(0, 10);
        Remboursement refund = Remboursement.builder().id(UUID.randomUUID()).build();
        RemboursementResponse response = new RemboursementResponse(
                refund.getId(), UUID.randomUUID(), "RES-4", BigDecimal.ONE, "reason",
                LocalDateTime.now(clock), StatutRemboursement.PENDING);
        Page<Remboursement> refunds = new PageImpl<>(List.of(refund), pageable, 1);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(remboursementJpaRepository.findByPaiment_Reservation_Client_Id(clientId, pageable)).thenReturn(refunds);
        when(remboursementJpaRepository.findAll(pageable)).thenReturn(refunds);
        when(remboursementMapper.toResponse(refund)).thenReturn(response);

        assertEquals(List.of(response), remboursementService.list(pageable, request).getContent());
        assertEquals(List.of(response), remboursementService.fullList(pageable, request).getContent());
        verify(remboursementJpaRepository).findByPaiment_Reservation_Client_Id(clientId, pageable);
        verify(remboursementJpaRepository).findAll(pageable);
    }
}
