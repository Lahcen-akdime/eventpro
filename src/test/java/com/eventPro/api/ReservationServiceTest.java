package com.eventPro.api;

import com.eventPro.api.Config.ReservationProperties;
import com.eventPro.api.Dto.Reservation.LigneReservationRequest ;
import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.AucuneReservationTrouveeException;
import com.eventPro.api.Exception.InsufficientStockException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.ReservationLineJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito cree les @Mock avant chaque test
@MockitoSettings(strictness = Strictness.LENIENT) // pas d'erreur si un stub n'est pas utilise dans un test
class ReservationServiceTest {

    @Mock
    private ReservationJpaRepository reservationJpaRepository;
    @Mock
    private TicketCategoryRepository ticketCategoryRepository;
    @Mock
    private PromotionService promotionService;
    @Mock
    private ClientService clientService;
    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private ReservationLineJpaRepository reservationLineJpaRepository;
    @Mock
    private ReservationProperties reservationProperties;
    @Mock
    private HttpServletRequest servletRequest;

    // le service est cree "a la main" dans setUp() car il a besoin d'un vrai Clock (date fixe)
    private ReservationService service;

    // UserService.getAuthenticatedEmail(...) est une methode STATIQUE : on la simule avec MockedStatic
    private MockedStatic<UserService> userServiceStatic;

    private static final String EMAIL = "client@gmail.com";

    // date "maintenant" figee : 2026-11-13 10:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T10:00:00Z"), ZoneId.of("UTC"));
    private final LocalDateTime now = LocalDateTime.now(clock);

    private final UUID clientId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final UUID cat1Id = UUID.randomUUID();
    private final UUID cat2Id = UUID.randomUUID();
    private final UUID reservationId = UUID.randomUUID();

    private Client client;
    private Event event;
    private ReservationResponse response;

    @BeforeEach
    void setUp() {
        // ordre = ordre des champs final du service
        service = new ReservationService(reservationJpaRepository, ticketCategoryRepository, promotionService,
                clientService, userService, eventService, reservationMapper, reservationLineJpaRepository,
                reservationProperties, clock);

        userServiceStatic = mockStatic(UserService.class);
        userServiceStatic.when(() -> UserService.getAuthenticatedEmail(servletRequest)).thenReturn(EMAIL);

        client = mock(Client.class);
        when(client.getId()).thenReturn(clientId);
        when(clientService.findByEmail(EMAIL)).thenReturn(client);

        event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.isOnSale(now)).thenReturn(true); // vente ouverte par defaut
        when(eventService.findEvent(eventId)).thenReturn(event);

        when(reservationProperties.holdDuration()).thenReturn(Duration.ofMinutes(10));

        response = mock(ReservationResponse.class);
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(response);
        when(reservationJpaRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
        // pas de reservations deja prises par defaut
        when(reservationLineJpaRepository.sumActiveQuantity(any(), any(), any())).thenReturn(0L);
    }

    @AfterEach
    void tearDown() {
        userServiceStatic.close(); // obligatoire pour les mocks statiques
    }

    // ---------- petits helpers (comme OneUser() avant) ----------
    // categorie de l'event : "available" places, max "maxPerClient" par client, prix 100
    private TicketCategory OneCategory(UUID id, int available, int maxPerClient) {
        TicketCategory c = mock(TicketCategory.class);
        when(c.getId()).thenReturn(id);
        when(c.getEvent()).thenReturn(event);
        when(c.getAvailablePlaces()).thenReturn(available);
        when(c.getMaxPerClient()).thenReturn(maxPerClient);
        when(c.getUnitPrice()).thenReturn(new BigDecimal("100.00"));
        return c;
    }

    private LigneReservationRequest OneLine(UUID categoryId, int quantity) {
        LigneReservationRequest l = mock(LigneReservationRequest.class);
        when(l.categoryId()).thenReturn(categoryId);
        when(l.quantity()).thenReturn(quantity);
        return l;
    }

    private ReservationCreateRequest OneRequest(String promoCode, LigneReservationRequest... lines) {
        ReservationCreateRequest r = mock(ReservationCreateRequest.class);
        when(r.eventId()).thenReturn(eventId);
        when(r.lines()).thenReturn(List.of(lines));
        when(r.promoCode()).thenReturn(promoCode);
        return r;
    }

    // le lock des categories retourne ces categories
    private void stubLock(TicketCategory... categories) {
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(anyList())).thenReturn(List.of(categories));
    }

    // recupere la reservation envoyee a save()
    private Reservation reservationSauvegardee() {
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationJpaRepository).save(captor.capture());
        return captor.getValue();
    }

    // =====================================================================
    // create : cas nominaux
    // =====================================================================
    @Test
    void create_nominalSansPromo_creeUneReservationPendingPayment() {
        // ARRANGE
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 2));

        // ACT
        ReservationResponse result = service.create(request, servletRequest);

        // ASSERT
        assertSame(response, result);
        Reservation r = reservationSauvegardee();
        assertEquals(StatutReservation.PENDING_PAYMENT, r.getStatutReservation());
        assertNotNull(r.getReference());
        assertSame(client, r.getClient());
        assertSame(event, r.getEvent());
        assertEquals(now, r.getDateCreation());
        assertEquals(now.plusMinutes(10), r.getDateExpiration()); // now + holdDuration
        verify(vip).reserve(2);                                   // la place est reservee
        verifyNoInteractions(promotionService);
    }

    @Test
    void create_deuxCategories_reserveChacuneAvecSaQuantite() {
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        TicketCategory std = OneCategory(cat2Id, 100, 6);
        stubLock(vip, std);
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 1), OneLine(cat2Id, 3));

        service.create(request, servletRequest);

        verify(vip).reserve(1);
        verify(std).reserve(3);
        verify(reservationJpaRepository).save(any(Reservation.class));
    }

    @Test
    void create_verifieLeQuotaAvecLesStatutsActifs() {
        // les reservations deja prises comptent seulement si PENDING_PAYMENT ou CONFIRMED
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 1));

        service.create(request, servletRequest);

        verify(reservationLineJpaRepository).sumActiveQuantity(clientId, cat1Id,
                List.of(StatutReservation.PENDING_PAYMENT, StatutReservation.CONFIRMED));
    }

    @Test
    void create_stockExactementEgalALaQuantite_estAccepte() {
        TicketCategory vip = OneCategory(cat1Id, 2, 4); // 2 places, on en veut 2
        stubLock(vip);

        service.create(OneRequest(null, OneLine(cat1Id, 2)), servletRequest);

        verify(vip).reserve(2);
    }

    @Test
    void create_quotaExactementAtteint_estAccepte() {
        // deja 2 + 2 voulus = 4 = max
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        when(reservationLineJpaRepository.sumActiveQuantity(eq(clientId), eq(cat1Id), any())).thenReturn(2L);

        service.create(OneRequest(null, OneLine(cat1Id, 2)), servletRequest);

        verify(vip).reserve(2);
    }


    @Test
    void create_codePromoNull_neCherchePasDePromotion() {
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);

        service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest);

        verifyNoInteractions(promotionService);
    }

    @Test
    void create_codePromoVide_neCherchePasDePromotion() {
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);

        service.create(OneRequest("   ", OneLine(cat1Id, 1)), servletRequest);

        verifyNoInteractions(promotionService);
    }

    @Test
    void create_promotionNonApplicable_exceptionPropagee_etRienNEstSauvegarde() {
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        when(promotionService.findApplicablePromotion("BAD", now, event))
                .thenThrow(new InvalidOperationException("promo invalide"));

        assertThrows(InvalidOperationException.class,
                () -> service.create(OneRequest("BAD", OneLine(cat1Id, 1)), servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    // =====================================================================
    // create : erreurs
    // =====================================================================
    @Test
    void create_clientIntrouvable_exceptionPropagee_sansChercherLEvent() {
        when(clientService.findByEmail(EMAIL)).thenThrow(new InvalidVDataException("client introuvable"));

        assertThrows(InvalidVDataException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verifyNoInteractions(eventService);
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_utilisateurInactif_exceptionPropagee_etRienNEstSauvegarde() {
        doThrow(new InvalidOperationException("compte inactif")).when(userService).checkIsActive(EMAIL);

        assertThrows(InvalidOperationException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_eventIntrouvable_exceptionPropagee() {
        when(eventService.findEvent(eventId)).thenThrow(new InvalidVDataException("event introuvable"));

        assertThrows(InvalidVDataException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_ventePasOuverte_InvalidOperation_sansVerrouillerLesCategories() {
        when(event.isOnSale(now)).thenReturn(false);

        assertThrows(InvalidOperationException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verifyNoInteractions(ticketCategoryRepository);
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_memeCategorieDeuxFois_InvalidVData_avantLeVerrou() {
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 1), OneLine(cat1Id, 2));

        assertThrows(InvalidVDataException.class, () -> service.create(request, servletRequest));

        verifyNoInteractions(ticketCategoryRepository);
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_categorieIntrouvable_InvalidVData() {
        // on demande 2 categories, la base n'en retourne qu'une
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 1), OneLine(cat2Id, 1));

        assertThrows(InvalidVDataException.class, () -> service.create(request, servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_categorieDUnAutreEvent_InvalidVData() {
        Event autre = mock(Event.class);
        when(autre.getId()).thenReturn(UUID.randomUUID());
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        when(vip.getEvent()).thenReturn(autre);
        stubLock(vip);

        assertThrows(InvalidVDataException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_categorieSansEvent_InvalidVData() {
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        when(vip.getEvent()).thenReturn(null);
        stubLock(vip);

        assertThrows(InvalidVDataException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 1)), servletRequest));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_stockInsuffisant_InsufficientStock() {
        TicketCategory vip = OneCategory(cat1Id, 1, 4); // 1 place, on en veut 2
        stubLock(vip);

        assertThrows(InsufficientStockException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 2)), servletRequest));

        verify(vip, never()).reserve(anyInt());
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_stockInsuffisantSurLaDeuxiemeLigne_aucuneCategorieNEstReservee() {
        // les verifications se font pour toutes les lignes AVANT de reserver quoi que ce soit
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        TicketCategory std = OneCategory(cat2Id, 0, 6); // plus de place
        stubLock(vip, std);
        ReservationCreateRequest request = OneRequest(null, OneLine(cat1Id, 1), OneLine(cat2Id, 1));

        assertThrows(InsufficientStockException.class, () -> service.create(request, servletRequest));

        verify(vip, never()).reserve(anyInt());
        verify(std, never()).reserve(anyInt());
        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void create_quotaDepasse_InvalidOperation() {
        // deja 3 + 2 voulus = 5 > 4
        TicketCategory vip = OneCategory(cat1Id, 10, 4);
        stubLock(vip);
        when(reservationLineJpaRepository.sumActiveQuantity(eq(clientId), eq(cat1Id), any())).thenReturn(3L);

        assertThrows(InvalidOperationException.class,
                () -> service.create(OneRequest(null, OneLine(cat1Id, 2)), servletRequest));

        verify(vip, never()).reserve(anyInt());
        verify(reservationJpaRepository, never()).save(any());
    }

    // =====================================================================
    // getMyReservations
    // =====================================================================
    @Test
    void getMyReservations_retourneLaPageDuClientConnecte() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Reservation r1 = mock(Reservation.class);
        ReservationResponse resp1 = mock(ReservationResponse.class);
        when(reservationJpaRepository.getAllByClient(client, pageable)).thenReturn(new PageImpl<>(List.of(r1), pageable, 1));
        when(reservationMapper.toResponse(r1)).thenReturn(resp1);

        // ACT
        Page<ReservationResponse> page = service.getMyReservations(pageable, servletRequest);

        // ASSERT
        assertEquals(1, page.getContent().size());
        assertSame(resp1, page.getContent().get(0));
        verify(clientService).findByEmail(EMAIL); // le client vient de l'email authentifie
    }

    @Test
    void getMyReservations_aucuneReservation_pageVide() {
        Pageable pageable = PageRequest.of(0, 10);
        when(reservationJpaRepository.getAllByClient(client, pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ReservationResponse> page = service.getMyReservations(pageable, servletRequest);

        assertTrue(page.getContent().isEmpty());
    }

    // =====================================================================
    // getById / findById
    // =====================================================================
    @Test
    void getById_introuvable_InvalidVData() {
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(InvalidVDataException.class, () -> service.getById(reservationId));
    }

    @Test
    void getById_trouvee_retourneLaReponse() {
        Reservation r = mock(Reservation.class);
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(r));
        when(reservationMapper.toResponse(r)).thenReturn(response);

        assertSame(response, service.getById(reservationId));
    }

    @Test
    void findById_introuvable_InvalidVData() {
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(InvalidVDataException.class, () -> service.findById(reservationId));
    }

    @Test
    void findById_trouvee_retourneLEntite() {
        Reservation r = mock(Reservation.class);
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(r));

        assertSame(r, service.findById(reservationId));
    }

    // =====================================================================
    // cancel
    // =====================================================================
    @Test
    void cancel_introuvable_InvalidVData_sansSauvegarder() {
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(InvalidVDataException.class, () -> service.cancel(reservationId));

        verify(reservationJpaRepository, never()).save(any());
    }

    @Test
    void cancel_nominal_annuleEtSauvegarde() {
        Reservation r = mock(Reservation.class);
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(r));

        service.cancel(reservationId);

        verify(r).cancel();
        verify(reservationJpaRepository).save(r);
    }

    @Test
    void cancel_laRegleMetierRefuse_exceptionPropagee_sansSauvegarder() {
        // ex : reservation deja payee, cancel() de l'entite leve une exception
        Reservation r = mock(Reservation.class);
        doThrow(new InvalidOperationException("deja payee")).when(r).cancel();
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(r));

        assertThrows(InvalidOperationException.class, () -> service.cancel(reservationId));

        verify(reservationJpaRepository, never()).save(any());
    }

    // =====================================================================
    // getList / findByIdForUpdate
    // =====================================================================
    @Test
    void getList_retourneLaPageMappee() {
        Pageable pageable = PageRequest.of(0, 3);
        Reservation r1 = mock(Reservation.class);
        ReservationResponse resp1 = mock(ReservationResponse.class);
        when(reservationJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(r1), pageable, 1));
        when(reservationMapper.toResponse(r1)).thenReturn(resp1);

        Page<ReservationResponse> page = service.getList(pageable);

        assertEquals(1, page.getContent().size());
        assertSame(resp1, page.getContent().get(0));
    }

    @Test
    void findByIdForUpdate_trouvee_retourneLOptional() {
        Reservation r = mock(Reservation.class);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(r));

        Optional<Reservation> result = service.findByIdForUpdate(reservationId);

        assertTrue(result.isPresent());
        assertSame(r, result.get());
    }

    @Test
    void findByIdForUpdate_introuvable_retourneUnOptionalVide_sansException() {
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.empty());

        assertTrue(service.findByIdForUpdate(reservationId).isEmpty());
    }

    // =====================================================================
    // getReservationByEventId
    // =====================================================================
    @Test
    void getReservationByEventId_retourneLesReservationsMappees() {
        // ARRANGE
        Reservation r1 = mock(Reservation.class);
        Reservation r2 = mock(Reservation.class);
        ReservationResponse resp1 = mock(ReservationResponse.class);
        ReservationResponse resp2 = mock(ReservationResponse.class);
        when(reservationJpaRepository.findAllByEventIdAndPublishedState(event)).thenReturn(List.of(r1, r2));
        when(reservationMapper.toResponse(r1)).thenReturn(resp1);
        when(reservationMapper.toResponse(r2)).thenReturn(resp2);

        // ACT
        List<ReservationResponse> result = service.getReservationByEventId(eventId);

        // ASSERT
        assertEquals(2, result.size());
        assertSame(resp1, result.get(0));
        assertSame(resp2, result.get(1));
    }

    @Test
    void getReservationByEventId_eventIntrouvable_exceptionPropagee() {
        when(eventService.findEvent(eventId)).thenThrow(new InvalidVDataException("event introuvable"));

        assertThrows(InvalidVDataException.class, () -> service.getReservationByEventId(eventId));

        verifyNoInteractions(reservationJpaRepository);
    }

    @Test
    void getReservationByEventId_aucuneReservation_listeVide() {
        when(reservationJpaRepository.findAllByEventIdAndPublishedState(event)).thenReturn(List.of());

        assertTrue(service.getReservationByEventId(eventId).isEmpty());
    }

    // =====================================================================
    // findAllByClientEmailAndCategoryNameAndQuantiteMin  (mise en situation)
    // =====================================================================
    @Test
    void recherche_nominal_retourneLesReservationsMappees() {
        // ARRANGE
        Reservation r1 = mock(Reservation.class);
        when(reservationJpaRepository.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 3L))
                .thenReturn(List.of(r1));
        when(reservationMapper.toResponse(r1)).thenReturn(response);

        // ACT
        List<ReservationResponse> result = service.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 3L);

        // ASSERT
        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }

    @Test
    void recherche_quantiteNull_utiliseLaValeurParDefaut1() {
        Reservation r1 = mock(Reservation.class);
        when(reservationJpaRepository.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 1L))
                .thenReturn(List.of(r1));
        when(reservationMapper.toResponse(r1)).thenReturn(response);

        List<ReservationResponse> result = service.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", null);

        assertEquals(1, result.size());
        verify(reservationJpaRepository).findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 1L);
    }

    @Test
    void recherche_aucuneReservation_AucuneReservationTrouvee() {
        when(reservationJpaRepository.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 5L))
                .thenReturn(List.of());

        assertThrows(AucuneReservationTrouveeException.class,
                () -> service.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 5L));

        verifyNoInteractions(reservationMapper);
    }

    @Test
    void recherche_aucuneReservationAvecQuantiteNull_leveAussiLException() {
        when(reservationJpaRepository.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", 1L))
                .thenReturn(List.of());

        assertThrows(AucuneReservationTrouveeException.class,
                () -> service.findAllByClientEmailAndCategoryNameAndQuantiteMin(EMAIL, "VIP", null));
    }
}