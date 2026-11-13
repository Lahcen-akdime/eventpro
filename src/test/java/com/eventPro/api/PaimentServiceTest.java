package com.eventPro.api;

import com.eventPro.api.Dto.Paiment.PaimentRequest;
import com.eventPro.api.Dto.Paiment.PaimentResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.Ticket;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.AccessDeniedException;
import com.eventPro.api.Exception.IdempotencyConflictException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Exception.ReservationExpiredException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.PaiementMapper;
import com.eventPro.api.Repository.PaimentJpaRepository;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Repository.TicketJpaRepository;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.PaimentService;
import com.eventPro.api.Service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT) // pas d'erreur si un stub n'est pas utilise dans un test
class PaimentServiceTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private ClientService clientService;
    @Mock
    private PaimentJpaRepository paimentJpaRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private ReservationJpaRepository reservationJpaRepository;
    @Mock
    private TicketJpaRepository ticketJpaRepository;
    @Mock
    private PaiementMapper paiementMapper;
    @Mock
    private HttpServletRequest httpRequest;

    @Captor
    private ArgumentCaptor<List<Ticket>> ticketsCaptor;

    // le service est cree "a la main" dans setUp() car on lui donne un vrai Clock (date fixe)
    private PaimentService service;

    // date "maintenant" figee : 2026-11-13 10:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDateTime now = LocalDateTime.now(clock);

    private static final String KEY = "key-1";

    private final UUID reservationId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final UUID cat1Id = UUID.randomUUID();
    private final UUID cat2Id = UUID.randomUUID();
    private final BigDecimal total = new BigDecimal("650.00");

    private Client client;
    private PaimentResponse response;

    @BeforeEach
    void setUp() {
        // ordre = ordre des champs final du service
        service = new PaimentService(reservationService, clock, clientService, paimentJpaRepository,
                ticketCategoryRepository, reservationJpaRepository, ticketJpaRepository, paiementMapper);

        client = mock(Client.class);
        when(client.getId()).thenReturn(clientId);
        when(clientService.getAuthaunticatedClient(httpRequest)).thenReturn(client);

        response = mock(PaimentResponse.class);
        when(paiementMapper.toResponse(any(Paiment.class), anyList())).thenReturn(response);
        when(paimentJpaRepository.save(any(Paiment.class))).thenAnswer(i -> i.getArgument(0));
        when(ticketJpaRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(paimentJpaRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
    }

    // ---------- petits helpers (comme OneUser() avant) ----------
    private PaimentRequest OneRequest(String key, String amount) {
        PaimentRequest r = mock(PaimentRequest.class);
        when(r.idempotencyKey()).thenReturn(key);
        when(r.amount()).thenReturn(new BigDecimal(amount));
        return r;
    }

    private PaimentRequest RequestValide() {
        return OneRequest(KEY, "650.00");
    }

    private TicketCategory OneCategory(UUID id) {
        TicketCategory c = mock(TicketCategory.class);
        when(c.getId()).thenReturn(id);
        return c;
    }

    private LigneReservation OneLine(TicketCategory category, int quantite) {
        LigneReservation l = mock(LigneReservation.class);
        when(l.getTicketCategory()).thenReturn(category);
        when(l.getQuantite()).thenReturn(quantite);
        return l;
    }

    private Reservation OneReservation(StatutReservation statut, boolean expiree, List<LigneReservation> lines) {
        Reservation r = mock(Reservation.class);
        when(r.getId()).thenReturn(reservationId);
        when(r.getClient()).thenReturn(client);
        when(r.getStatutReservation()).thenReturn(statut);
        when(r.isExpiredAt(now)).thenReturn(expiree);
        when(r.getTotalAmount()).thenReturn(total);
        when(r.getReference()).thenReturn("REF-TEST");
        when(r.getLines()).thenReturn(lines);
        return r;
    }

    private void stubReservation(Reservation reservation) {
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
    }

    // reservation payable avec 2 lignes (2 billets VIP + 1 billet Standard), categories verrouillables
    private Reservation ReservationPayable(TicketCategory vip, TicketCategory std) {
        List<LigneReservation> lines = List.of(OneLine(vip, 2), OneLine(std, 1));
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, false, lines);
        stubReservation(reservation);
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(anyList())).thenReturn(List.of(vip, std));
        return reservation;
    }

    // recupere le paiement envoye a save()
    private Paiment paiementSauvegarde() {
        ArgumentCaptor<Paiment> captor = ArgumentCaptor.forClass(Paiment.class);
        verify(paimentJpaRepository).save(captor.capture());
        return captor.getValue();
    }

    // =====================================================================
    // pay : succes
    // =====================================================================
    @Test
    void pay_succes_sauvegardeUnPaiementSuccess_etConfirmeLaReservation() {
        // ARRANGE
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        Reservation reservation = ReservationPayable(vip, std);

        // ACT
        PaimentResponse result = service.pay(reservationId, RequestValide(), httpRequest);

        // ASSERT
        assertSame(response, result);
        Paiment p = paiementSauvegarde();
        assertEquals(StatutPaiement.SUCCESS, p.getStatut());
        assertTrue(p.getReference().startsWith("PAY-"));
        assertEquals(4 + 12, p.getReference().length()); // "PAY-" + 12 caracteres
        assertEquals(KEY, p.getIdempotencyKey());
        assertEquals(0, p.getAmount().compareTo(new BigDecimal("650.00")));
        assertEquals(now, p.getPaidAt());
        assertSame(reservation, p.getReservation());
        verify(reservation).confirm(now);
        verify(reservationJpaRepository).save(reservation);
    }

    @Test
    void pay_succes_confirmeLaVenteDeChaqueCategorieAvecSaQuantite() {
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        service.pay(reservationId, RequestValide(), httpRequest);

        verify(vip).confirmSale(2);
        verify(std).confirmSale(1);
    }

    @Test
    void pay_succes_genereUnTicketParPlace_toutesValid() {
        // ARRANGE : lignes 2 + 1 = 3 tickets
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        // ACT
        service.pay(reservationId, RequestValide(), httpRequest);

        // ASSERT
        verify(ticketJpaRepository).saveAll(ticketsCaptor.capture());
        List<Ticket> tickets = ticketsCaptor.getValue();
        assertEquals(3, tickets.size());
        int pourVip = 0;
        int pourStandard = 0;
        for (Ticket t : tickets) {
            assertEquals(TicketStatus.VALID, t.getStatus());
            assertNotNull(t.getCode());
            if (t.getTicketCategory() == vip) {
                pourVip++;
            }
            if (t.getTicketCategory() == std) {
                pourStandard++;
            }
        }
        assertEquals(2, pourVip);
        assertEquals(1, pourStandard);
    }

    @Test
    void pay_succes_retourneLaReponseDuMapperAvecLesTicketsSauvegardes() {
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        service.pay(reservationId, RequestValide(), httpRequest);

        // le mapper recoit le paiement sauvegarde et la liste retournee par saveAll
        verify(ticketJpaRepository).saveAll(ticketsCaptor.capture());
        List<Ticket> tickets = ticketsCaptor.getValue();
        Paiment p = paiementSauvegarde(); // dans une variable : jamais un verify() a l'interieur d'un autre verify()
        verify(paiementMapper).toResponse(p, tickets);
    }

    @Test
    void pay_deuxLignesDeLaMemeCategorie_verrouilleLaCategorieUneSeuleFois() {
        // ARRANGE : 2 lignes sur la meme categorie -> la liste d'ids est "distinct"
        TicketCategory vip = OneCategory(cat1Id);
        List<LigneReservation> lines = List.of(OneLine(vip, 1), OneLine(vip, 2));
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, false, lines);
        stubReservation(reservation);
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(anyList())).thenReturn(List.of(vip));

        // ACT
        service.pay(reservationId, RequestValide(), httpRequest);

        // ASSERT
        verify(ticketCategoryRepository).findAllByIdInOrderByIdForUpdate(List.of(cat1Id));
        verify(vip).confirmSale(1);
        verify(vip).confirmSale(2);
        verify(ticketJpaRepository).saveAll(ticketsCaptor.capture());
        assertEquals(3, ticketsCaptor.getValue().size());
    }

    @Test
    void pay_montantEgalAvecUneAutreEchelle_estAccepte() {
        // 650 vs 650.00 : comparaison avec compareTo
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        service.pay(reservationId, OneRequest(KEY, "650"), httpRequest);

        assertEquals(StatutPaiement.SUCCESS, paiementSauvegarde().getStatut());
    }

    // =====================================================================
    // pay : echec simule (cle qui commence par FAIL)
    // =====================================================================
    @Test
    void pay_cleQuiCommenceParFAIL_sauvegardeUnPaiementFailed_sansTickets() {
        // ARRANGE
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        Reservation reservation = ReservationPayable(vip, std);

        // ACT
        PaimentResponse result = service.pay(reservationId, OneRequest("FAIL-123", "650.00"), httpRequest);

        // ASSERT
        assertSame(response, result);
        Paiment p = paiementSauvegarde();
        assertEquals(StatutPaiement.FAILED, p.getStatut());
        assertEquals("FAIL-123", p.getIdempotencyKey());
        assertTrue(p.getReference().startsWith("PAY-"));
        assertSame(reservation, p.getReservation());
        verify(paiementMapper).toResponse(p, List.of()); // aucun ticket
        verify(reservation, never()).confirm(any());
        verify(ticketJpaRepository, never()).saveAll(anyList());
        verifyNoInteractions(ticketCategoryRepository);
        verify(vip, never()).confirmSale(anyInt());
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void pay_cleFAILAvecMauvaisMontant_InvalidVData_laValidationPasseAvant() {
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        assertThrows(InvalidVDataException.class,
                () -> service.pay(reservationId, OneRequest("FAIL-1", "100.00"), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    // =====================================================================
    // pay : idempotence
    // =====================================================================
    @Test
    void pay_replayMemeReservationEtMemeMontant_retourneLaReponse_sansRepayer() {
        // ARRANGE
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        Reservation reservation = ReservationPayable(vip, std);
        Paiment existant = mock(Paiment.class);
        when(existant.getReservation()).thenReturn(reservation);
        when(existant.getAmount()).thenReturn(new BigDecimal("650.0")); // echelle differente
        List<Ticket> tickets = List.of(mock(Ticket.class));
        when(paimentJpaRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existant));
        when(ticketJpaRepository.findAllByReservationLine_Reservation_Id(reservationId)).thenReturn(tickets);
        PaimentResponse existante = mock(PaimentResponse.class);
        when(paiementMapper.toResponse(existant, tickets)).thenReturn(existante);

        // ACT
        PaimentResponse result = service.pay(reservationId, RequestValide(), httpRequest);

        // ASSERT : on rejoue la reponse, aucun nouveau paiement ni ticket
        assertSame(existante, result);
        verify(paimentJpaRepository, never()).save(any());
        verify(ticketJpaRepository, never()).saveAll(anyList());
        verify(reservation, never()).confirm(any());
    }

    @Test
    void pay_replayMemeSiLaReservationEstDejaConfirmee_retourneLaReponse() {
        // l'idempotence est verifiee AVANT le statut : un 2eme appel identique ne doit pas echouer
        Reservation reservation = OneReservation(StatutReservation.CONFIRMED, false, List.of());
        stubReservation(reservation);
        Paiment existant = mock(Paiment.class);
        when(existant.getReservation()).thenReturn(reservation);
        when(existant.getAmount()).thenReturn(total);
        when(paimentJpaRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existant));
        PaimentResponse existante = mock(PaimentResponse.class);
        when(paiementMapper.toResponse(eq(existant), anyList())).thenReturn(existante);

        PaimentResponse result = service.pay(reservationId, RequestValide(), httpRequest);

        assertSame(existante, result);
    }

    @Test
    void pay_replayAvecUneAutreReservation_IdempotencyConflict() {
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, false, List.of());
        stubReservation(reservation);
        Reservation autre = mock(Reservation.class);
        when(autre.getId()).thenReturn(UUID.randomUUID());
        Paiment existant = mock(Paiment.class);
        when(existant.getReservation()).thenReturn(autre);
        when(existant.getAmount()).thenReturn(total);
        when(paimentJpaRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existant));

        assertThrows(IdempotencyConflictException.class,
                () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    @Test
    void pay_replayAvecUnAutreMontant_IdempotencyConflict() {
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, false, List.of());
        stubReservation(reservation);
        Paiment existant = mock(Paiment.class);
        when(existant.getReservation()).thenReturn(reservation);
        when(existant.getAmount()).thenReturn(new BigDecimal("100.00"));
        when(paimentJpaRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existant));

        assertThrows(IdempotencyConflictException.class,
                () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    // =====================================================================
    // pay : erreurs
    // =====================================================================
    @Test
    void pay_reservationIntrouvable_ResourceNotFound() {
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    @Test
    void pay_pasLeProprietaire_AccessDenied_avantTouteAutreVerification() {
        // la reservation appartient a un autre client
        Client autreClient = mock(Client.class);
        when(autreClient.getId()).thenReturn(UUID.randomUUID());
        Reservation reservation = mock(Reservation.class);
        when(reservation.getClient()).thenReturn(autreClient);
        stubReservation(reservation);

        assertThrows(AccessDeniedException.class, () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).findByIdempotencyKey(any());
        verify(paimentJpaRepository, never()).save(any());
    }

    @Test
    void pay_reservationDejaConfirmee_InvalidOperation() {
        Reservation reservation = OneReservation(StatutReservation.CONFIRMED, false, List.of());
        stubReservation(reservation);

        assertThrows(InvalidOperationException.class, () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    @Test
    void pay_reservationExpiree_ReservationExpired() {
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, true, List.of());
        stubReservation(reservation);

        assertThrows(ReservationExpiredException.class,
                () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
    }

    @Test
    void pay_mauvaisMontant_InvalidVData_sansRienSauvegarder() {
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        ReservationPayable(vip, std);

        assertThrows(InvalidVDataException.class,
                () -> service.pay(reservationId, OneRequest(KEY, "100.00"), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
        verify(ticketJpaRepository, never()).saveAll(anyList());
        verifyNoInteractions(ticketCategoryRepository);
    }

    @Test
    void pay_categorieIntrouvable_IllegalState_etRienNEstSauvegarde() {
        // 2 categories dans la reservation, la base n'en retourne qu'une
        TicketCategory vip = OneCategory(cat1Id);
        TicketCategory std = OneCategory(cat2Id);
        List<LigneReservation> lines = List.of(OneLine(vip, 2), OneLine(std, 1));
        Reservation reservation = OneReservation(StatutReservation.PENDING_PAYMENT, false, lines);
        stubReservation(reservation);
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(anyList())).thenReturn(List.of(vip));

        assertThrows(IllegalStateException.class, () -> service.pay(reservationId, RequestValide(), httpRequest));

        verify(paimentJpaRepository, never()).save(any());
        verify(reservation, never()).confirm(any());
        verify(ticketJpaRepository, never()).saveAll(anyList());
    }

    // =====================================================================
    // getById
    // =====================================================================
    @Test
    void getById_paiementTrouve_retourneLaReponse() {
        // ARRANGE
        UUID paimentId = UUID.randomUUID();
        Paiment paiment = mock(Paiment.class);
        List<Ticket> tickets = List.of(mock(Ticket.class));
        when(paimentJpaRepository.findById(paimentId)).thenReturn(Optional.of(paiment));
        when(ticketJpaRepository.findAllByClientId(clientId)).thenReturn(tickets);
        when(paiementMapper.toResponse(paiment, tickets)).thenReturn(response);

        // ACT
        PaimentResponse result = service.getById(paimentId, httpRequest);

        // ASSERT
        assertSame(response, result);
        verify(clientService).getAuthaunticatedClient(httpRequest);
    }

    @Test
    void getById_paiementIntrouvable_ResourceNotFound() {
        UUID paimentId = UUID.randomUUID();
        when(paimentJpaRepository.findById(paimentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(paimentId, httpRequest));

        verify(ticketJpaRepository, never()).findAllByClientId(any());
    }
}