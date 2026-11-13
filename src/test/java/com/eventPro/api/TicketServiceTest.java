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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
class TicketServiceTest {

    @Mock
    private TicketJpaRepository ticketJpaRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private ClientService clientService;

    // le service est cree "a la main" dans setUp() car on lui donne un vrai Clock (date fixe)
    private TicketService ticketService;

    // date "maintenant" figee : 2026-11-12 12:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);

    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // ordre = ordre des champs final du service
        ticketService = new TicketService(ticketJpaRepository, ticketMapper, clientService, clock);
    }

    // ---------- petits helpers (comme OneUser() avant) ----------
    // ticket rattache a l'event "eventId", dont la reservation et le ticket ont les statuts voulus
    private Ticket OneTicket(UUID eventId, StatutReservation reservationStatus, TicketStatus ticketStatus) {
        Event event = Event.builder().id(eventId).build();
        TicketCategory category = TicketCategory.builder().event(event).build();
        return buildTicket(category, reservationStatus, ticketStatus);
    }

    // ticket dont la categorie n'a aucun event
    private Ticket OneTicketSansEvent() {
        TicketCategory category = TicketCategory.builder().event(null).build();
        return buildTicket(category, StatutReservation.CONFIRMED, TicketStatus.VALID);
    }

    private Ticket buildTicket(TicketCategory category, StatutReservation reservationStatus, TicketStatus ticketStatus) {
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

    // =====================================================================
    // verify
    // =====================================================================
    @Test
    void verify_ticketValideEtReservationConfirmee_retourneValid() {
        // ARRANGE
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-1")).thenReturn(Optional.of(ticket));

        // ACT
        TicketVerificationResponse result = ticketService.verify(eventId, "TICKET-1");

        // ASSERT
        assertTrue(result.valid());
        assertEquals("Ticket valide", result.message());
        assertEquals(TicketStatus.VALID, result.ticketState());
    }

    @Test
    void verify_ticketDejaUtilise_retourneInvalid_sansLeModifier() {
        // ARRANGE
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.USED);
        when(ticketJpaRepository.findByCode("TICKET-USED")).thenReturn(Optional.of(ticket));

        // ACT
        TicketVerificationResponse result = ticketService.verify(eventId, "TICKET-USED");

        // ASSERT
        assertFalse(result.valid());
        assertEquals("Ticket deja utilise ou invalide", result.message());
        assertEquals(TicketStatus.USED, result.ticketState());
        assertEquals(TicketStatus.USED, ticket.getStatus()); // le ticket reste USED
    }

    @Test
    void verify_neModifiePasLeTicket_etNeVerrouillePasLaLigne() {
        // verify = lecture seule : pas de findByCodeForUpdate, le ticket reste VALID, pas de date de scan
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-1")).thenReturn(Optional.of(ticket));

        ticketService.verify(eventId, "TICKET-1");

        assertEquals(TicketStatus.VALID, ticket.getStatus());
        assertNull(ticket.getCheckedAt());
        verify(ticketJpaRepository, never()).findByCodeForUpdate(any());
    }

    @Test
    void verify_ticketInexistant_ResourceNotFound() {
        when(ticketJpaRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(eventId, "UNKNOWN"));
    }

    @Test
    void verify_ticketDUnAutreEvent_ResourceNotFound() {
        UUID requestedEventId = UUID.randomUUID();
        Ticket ticket = OneTicket(UUID.randomUUID(), StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-3")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(requestedEventId, "TICKET-3"));
    }

    @Test
    void verify_categorieSansEvent_ResourceNotFound() {
        Ticket ticket = OneTicketSansEvent();
        when(ticketJpaRepository.findByCode("TICKET-NOEVENT")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(eventId, "TICKET-NOEVENT"));
    }

    @Test
    void verify_reservationNonConfirmee_InvalidOperation() {
        Ticket ticket = OneTicket(eventId, StatutReservation.PENDING_PAYMENT, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-4")).thenReturn(Optional.of(ticket));

        assertThrows(InvalidOperationException.class, () -> ticketService.verify(eventId, "TICKET-4"));
    }

    @Test
    void verify_autreEventEtReservationNonConfirmee_lErreurEventPasseEnPremier() {
        // les deux problemes a la fois : c'est "ticket pas trouve pour cet event" qui sort
        UUID requestedEventId = UUID.randomUUID();
        Ticket ticket = OneTicket(UUID.randomUUID(), StatutReservation.PENDING_PAYMENT, TicketStatus.VALID);
        when(ticketJpaRepository.findByCode("TICKET-5")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.verify(requestedEventId, "TICKET-5"));
    }

    // =====================================================================
    // scan
    // =====================================================================
    @Test
    void scan_ticketValide_marqueUsed_avecLaDateDuScan() {
        // ARRANGE
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-2")).thenReturn(Optional.of(ticket));

        // ACT
        TicketVerificationResponse result = ticketService.scan(eventId, "TICKET-2");

        // ASSERT
        assertTrue(result.valid());
        assertEquals("Ticket scanne avec succes", result.message());
        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertEquals(LocalDateTime.of(2026, 11, 12, 12, 0), ticket.getCheckedAt());
    }

    @Test
    void scan_utiliseLeVerrouEnEcriture_pasLaLectureSimple() {
        // scan doit verrouiller la ligne (findByCodeForUpdate) pour eviter un double scan simultane
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-2")).thenReturn(Optional.of(ticket));

        ticketService.scan(eventId, "TICKET-2");

        verify(ticketJpaRepository).findByCodeForUpdate("TICKET-2");
        verify(ticketJpaRepository, never()).findByCode(any());
    }

    @Test
    void scan_ticketInexistant_ResourceNotFound() {
        when(ticketJpaRepository.findByCodeForUpdate("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.scan(eventId, "UNKNOWN"));
    }

    @Test
    void scan_ticketDUnAutreEvent_ResourceNotFound_etResteValid() {
        UUID requestedEventId = UUID.randomUUID();
        Ticket ticket = OneTicket(UUID.randomUUID(), StatutReservation.CONFIRMED, TicketStatus.VALID);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-3")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.scan(requestedEventId, "TICKET-3"));

        assertEquals(TicketStatus.VALID, ticket.getStatus()); // pas compostee
    }

    @Test
    void scan_categorieSansEvent_ResourceNotFound() {
        Ticket ticket = OneTicketSansEvent();
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-NOEVENT")).thenReturn(Optional.of(ticket));

        assertThrows(ResourceNotFoundException.class, () -> ticketService.scan(eventId, "TICKET-NOEVENT"));
    }

    @Test
    void scan_reservationNonConfirmee_InvalidOperation_etTicketNonComposte() {
        Ticket ticket = OneTicket(eventId, StatutReservation.PENDING_PAYMENT, TicketStatus.VALID);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-4")).thenReturn(Optional.of(ticket));

        assertThrows(InvalidOperationException.class, () -> ticketService.scan(eventId, "TICKET-4"));

        assertEquals(TicketStatus.VALID, ticket.getStatus());
        assertNull(ticket.getCheckedAt());
    }

    @Test
    void scan_ticketDejaUtilise_TicketAlreadyUsed() {
        // suppose que Ticket.check() leve TicketAlreadyUsedException quand le ticket est deja USED
        Ticket ticket = OneTicket(eventId, StatutReservation.CONFIRMED, TicketStatus.USED);
        when(ticketJpaRepository.findByCodeForUpdate("TICKET-USED")).thenReturn(Optional.of(ticket));

        assertThrows(TicketAlreadyUsedException.class, () -> ticketService.scan(eventId, "TICKET-USED"));
    }

    // =====================================================================
    // getAll
    // =====================================================================
    @Test
    void getAll_retourneLesTicketsDuClientAuthentifie() {
        // ARRANGE
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

        // ACT
        List<TicketResponse> result = ticketService.getAll(request);

        // ASSERT
        assertEquals(List.of(response), result);
    }

    @Test
    void getAll_plusieursTickets_gardeLOrdreDuRepository() {
        UUID clientId = UUID.randomUUID();
        Client client = mock(Client.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        Ticket t1 = mock(Ticket.class);
        Ticket t2 = mock(Ticket.class);
        TicketResponse r1 = new TicketResponse(UUID.randomUUID(), "TICKET-A", TicketStatus.VALID, "VIP", "Event");
        TicketResponse r2 = new TicketResponse(UUID.randomUUID(), "TICKET-B", TicketStatus.USED, "Standard", "Event");
        when(client.getId()).thenReturn(clientId);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(ticketJpaRepository.findAllByClientId(clientId)).thenReturn(List.of(t1, t2));
        when(ticketMapper.toResponse(t1)).thenReturn(r1);
        when(ticketMapper.toResponse(t2)).thenReturn(r2);

        List<TicketResponse> result = ticketService.getAll(request);

        assertEquals(List.of(r1, r2), result);
    }

    @Test
    void getAll_clientSansTicket_retourneUneListeVide() {
        UUID clientId = UUID.randomUUID();
        Client client = mock(Client.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(client.getId()).thenReturn(clientId);
        when(clientService.getAuthaunticatedClient(request)).thenReturn(client);
        when(ticketJpaRepository.findAllByClientId(clientId)).thenReturn(List.of());

        assertTrue(ticketService.getAll(request).isEmpty());
        verifyNoInteractions(ticketMapper);
    }
}