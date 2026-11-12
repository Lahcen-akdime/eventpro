package com.eventPro.api;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Dto.Ticket.TicketVerificationResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.Ticket;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Exception.TicketAlreadyUsedException;
import com.eventPro.api.Mapper.TicketMapper;
import com.eventPro.api.Repository.TicketJpaRepository;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketJpaRepository ticketJpaRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private ClientService clientService;
    @Mock
    private Clock clock;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void returnsValidTicketForItsConfirmedEvent() {
        UUID eventId = UUID.randomUUID();
        Ticket ticket = ticketFor(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-1")).thenReturn(Optional.of(ticket));

        TicketVerificationResponse result = ticketService.verify(eventId, "TICKET-1");

        assertTrue(result.valid());
        assertEquals("Ticket valide", result.message());
        assertEquals(TicketStatus.VALID, result.ticketState());
    }

    @Test
    void verificationMarksUsedTicketAsInvalidWithoutChangingIt() {
        UUID eventId = UUID.randomUUID();
        Ticket ticket = ticketFor(eventId, StatutReservation.CONFIRMED, TicketStatus.USED);
        when(ticketJpaRepository.findByCode("TICKET-USED")).thenReturn(Optional.of(ticket));

        TicketVerificationResponse result = ticketService.verify(eventId, "TICKET-USED");

        assertFalse(result.valid());
        assertEquals("Ticket deja utilise ou invalide", result.message());
        assertEquals(TicketStatus.USED, result.ticketState());
    }

    @Test
    void scanMarksValidTicketAsUsed() {
        UUID eventId = UUID.randomUUID();
        Ticket ticket = ticketFor(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-2")).thenReturn(Optional.of(ticket));
        when(clock.instant()).thenReturn(Instant.parse("2026-11-12T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        TicketVerificationResponse result = ticketService.scan(eventId, "TICKET-2");

        assertTrue(result.valid());
        assertEquals("Ticket scanne avec succes", result.message());
        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertEquals(LocalDateTime.of(2026, 11, 12, 12, 0), ticket.getCheckedAt());
    }

    @Test
    void failsWhenTicketDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        when(ticketJpaRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(eventId, "UNKNOWN"));
    }

    @Test
    void rejectsTicketForAnotherEvent() {
        UUID requestedEventId = UUID.randomUUID();
        Ticket ticket = ticketFor(UUID.randomUUID(), StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-3")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(requestedEventId, "TICKET-3"));
    }

    @Test
    void rejectsTicketWhenReservationIsNotConfirmed() {
        UUID eventId = UUID.randomUUID();
        Ticket ticket = ticketFor(eventId, StatutReservation.PENDING_PAYMENT, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-4")).thenReturn(Optional.of(ticket));

        assertThrows(InvalidOperationException.class, () -> ticketService.verify(eventId, "TICKET-4"));
    }



    @Test
    void listsAuthenticatedClientsTickets() {
        UUID clientId = UUID.randomUUID();
        Client client = mock(Client.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Ticket ticket = mock(Ticket.class);
        TicketResponse response = new TicketResponse(
                UUID.randomUUID(), "TICKET-5", TicketStatus.VALID, "Standard", "Event");
        when(client.getId()).thenReturn(clientId);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(ticketJpaRepository.findAllByClientId(clientId)).thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response);

        assertEquals(List.of(response), ticketService.getAll(request));
    }

    private Ticket ticketFor(UUID eventId, StatutReservation reservationStatus, TicketStatus ticketStatus) {
        Event event = Event.builder().id(eventId).build();
        TicketCategory category = TicketCategory.builder().event(event).build();
        Reservation reservation = Reservation.builder().statutReservation(reservationStatus).build();
        LigneReservation line = LigneReservation.builder().reservation(reservation).build();
        return Ticket.builder()
                .id(UUID.randomUUID())
                .code("TICKET-TEST")
                .status(ticketStatus)
                .ticketCategory(category)
                .reservationLine(line)
                .build();
    }
}
