package com.eventPro.api;

import com.eventPro.api.Dto.Reservation.LigneReservationRequest;
import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Config.ReservationProperties;
import com.eventPro.api.Exception.InsufficientStockException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.ReservationLineJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReservationServiceTest {

    private static final Instant NOW_INSTANT = Instant.parse("2026-11-04T12:40:00Z");
    //private static final LocalDateTime NOW  = LocalDateTime.ofInstant(NOW_INSTANT,ZoneOffset.UTC) ;

    @Mock ReservationJpaRepository reservationJpaRepository ;
    @Mock TicketCategoryRepository ticketCategoryRepository ;
    @Mock PromotionService promotionService ;
    @Mock ClientService clientService ;
    @Mock UserService userService ;
    @Mock EventService eventService ;
    @Mock ReservationMapper reservationMapper ;
    @Mock ReservationLineJpaRepository reservationLineJpaRepository ;
    @Mock ReservationProperties reservationProperties ;
    @Mock private Clock clock ;
    @Mock private HttpServletRequest servletRequest ;

    @InjectMocks ReservationService reservationService ;

    private UUID eventId = UUID.randomUUID() ;
    private Client client ;
    private Event event ;
    private TicketCategory ticketCategory ;


    @BeforeEach
    void init(){
       when(clock.instant()).thenReturn(NOW_INSTANT) ;
       when(clock.getZone()).thenReturn(ZoneOffset.UTC) ;
       when(reservationProperties.holdDuration()).thenReturn(Duration.ofMinutes(10));

       client = mock(Client.class) ;
       event = mock(Event.class) ;
       when(event.getId()).thenReturn(eventId) ;
       when(event.getTitle()).thenReturn("eventTest") ;
       when(event.isOnSale(any())).thenReturn(true) ;
       when(eventService.findEvent(eventId)).thenReturn(event) ;
       when(clientService.findByEmail("client@mail.com")).thenReturn(client) ;
       when(reservationJpaRepository.save(any(Reservation.class))).thenAnswer(invocation->invocation.getArgument(0)) ;
       when(reservationLineJpaRepository.sumActiveQuantity(any(), any(), any())).thenReturn(0L) ;

       ticketCategory = TicketCategory.builder()
                    .id(UUID.randomUUID())
                    .event(event)
                    .totalQuantity(10)
                    .reservedPlaces(0)
                    .soldPlaces(0)
                    .maxPerClient(4)
                    .unitPrice(new BigDecimal("100.0"))
                    .build() ;

        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(any())).thenReturn(List.of(ticketCategory)) ;

    }

    @Test 
    void createReservationSuccessTest(){
        try(MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)){
            mockedUser.when(()->userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");
            LigneReservationRequest line = new LigneReservationRequest(ticketCategory.getId(), 2);
            ReservationCreateRequest request = new ReservationCreateRequest(eventId,List.of(line),null) ;
            reservationService.create(request, null) ;
            verify(reservationJpaRepository).save(any(Reservation.class)) ;
            assertEquals(2, ticketCategory.getReservedPlaces()) ;     
        }
    }

    @Test 
    void createReservationWithInsuffisantStockTest(){
        try(MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)){
            mockedUser.when(()->userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");
            LigneReservationRequest line = new LigneReservationRequest(ticketCategory.getId(), 200);
            ReservationCreateRequest request = new ReservationCreateRequest(eventId,List.of(line),null) ;
            assertThrows(InsufficientStockException.class,()->reservationService.create(request, null)) ;
            verify(reservationJpaRepository,never()).save(any(Reservation.class)) ;
            assertEquals(0,ticketCategory.getReservedPlaces()) ;
        }
    }

    @Test
    void createsReservationWithPromotionAndPersistsCalculatedAmounts() {
        Promotion promotion = mock(Promotion.class);
        when(promotionService.findApplicablePromotion("PROMO", LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC), event))
                .thenReturn(promotion);
        when(promotion.calculateDiscount(new BigDecimal("200.00"))).thenReturn(new BigDecimal("20.00"));
        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenReturn(new com.eventPro.api.Dto.Reservation.ReservationResponse(
                        null, null, null, null, null, null, null, null, null, null, null, List.of()));

        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");
            ReservationCreateRequest request = new ReservationCreateRequest(eventId,
                    List.of(new LigneReservationRequest(ticketCategory.getId(), 2)), " PROMO ");

            reservationService.create(request, servletRequest);

            ArgumentCaptor<Reservation> saved = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationJpaRepository).save(saved.capture());
            Reservation reservation = saved.getValue();
            assertEquals(StatutReservation.PENDING_PAYMENT, reservation.getStatutReservation());
            assertEquals(LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC), reservation.getDateCreation());
            assertEquals(LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC).plusMinutes(10),
                    reservation.getDateExpiration());
            assertEquals(new BigDecimal("200.00"), reservation.getSubtotal());
            assertEquals(new BigDecimal("20.00"), reservation.getDiscountAmount());
            assertEquals(new BigDecimal("180.00"), reservation.getTotalAmount());
            assertSame(promotion, reservation.getPromotion());
            assertEquals(2, reservation.getLines().get(0).getQuantite());
            assertSame(reservation, reservation.getLines().get(0).getReservation());
            assertEquals(2, ticketCategory.getReservedPlaces());
            verify(promotionService).findApplicablePromotion(
                    "PROMO", LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC), event);
        }
    }

    @Test
    void rejectsCreationWhenSaleIsClosed() {
        when(event.isOnSale(any())).thenReturn(false);
        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");

            assertThrows(InvalidOperationException.class, () -> reservationService.create(
                    new ReservationCreateRequest(eventId,
                            List.of(new LigneReservationRequest(ticketCategory.getId(), 1)), null), servletRequest));
            verifyNoInteractions(ticketCategoryRepository);
            verify(reservationJpaRepository, never()).save(any(Reservation.class));
        }
    }

    @Test
    void rejectsDuplicateCategoryLines() {
        LigneReservationRequest line = new LigneReservationRequest(ticketCategory.getId(), 1);
        ReservationCreateRequest request = new ReservationCreateRequest(eventId, List.of(line, line), null);
        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");

            assertThrows(InvalidVDataException.class, () -> reservationService.create(request, servletRequest));
            verifyNoInteractions(ticketCategoryRepository);
            verify(reservationJpaRepository, never()).save(any(Reservation.class));
        }
    }

    @Test
    void rejectsCategoryFromAnotherEvent() {
        Event anotherEvent = mock(Event.class);
        TicketCategory categoryFromAnotherEvent = TicketCategory.builder()
                .id(UUID.randomUUID())
                .event(anotherEvent)
                .totalQuantity(10)
                .reservedPlaces(0)
                .soldPlaces(0)
                .maxPerClient(4)
                .unitPrice(BigDecimal.TEN)
                .build();
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(any()))
                .thenReturn(List.of(categoryFromAnotherEvent));
        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");

            assertThrows(InvalidVDataException.class, () -> reservationService.create(
                    new ReservationCreateRequest(eventId,
                            List.of(new LigneReservationRequest(categoryFromAnotherEvent.getId(), 1)), null), servletRequest));
            verify(reservationJpaRepository, never()).save(any(Reservation.class));
        }
    }

    @Test
    void rejectsQuantityAboveClientLimit() {
        TicketCategory limitedCategory = TicketCategory.builder()
                .id(UUID.randomUUID())
                .event(event)
                .totalQuantity(10)
                .reservedPlaces(0)
                .soldPlaces(0)
                .maxPerClient(1)
                .unitPrice(BigDecimal.TEN)
                .build();
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(any())).thenReturn(List.of(limitedCategory));
        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");

            assertThrows(InvalidOperationException.class, () -> reservationService.create(
                    new ReservationCreateRequest(eventId,
                            List.of(new LigneReservationRequest(limitedCategory.getId(), 2)), null), servletRequest));
            verify(reservationJpaRepository, never()).save(any(Reservation.class));
            assertEquals(0, limitedCategory.getReservedPlaces());
        }
    }

    @Test
    void rejectsCreationWhenRequestedCategoryIsMissing() {
        when(ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(any())).thenReturn(List.of());
        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(any())).thenReturn("client@mail.com");

            assertThrows(InvalidVDataException.class, () -> reservationService.create(
                    new ReservationCreateRequest(eventId,
                            List.of(new LigneReservationRequest(UUID.randomUUID(), 1)), null), servletRequest));
            verify(reservationJpaRepository, never()).save(any(Reservation.class));
        }
    }

    @Test
    void getsAndCancelsReservations() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .statutReservation(StatutReservation.PENDING_PAYMENT)
                .build();
        var response = new com.eventPro.api.Dto.Reservation.ReservationResponse(
                reservationId, "REF-1", StatutReservation.PENDING_PAYMENT, eventId, "eventTest",
                null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, List.of());
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(response);
        when(reservationJpaRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        assertSame(response, reservationService.getById(reservationId));
        assertSame(reservation, reservationService.findById(reservationId));
        reservationService.cancel(reservationId);
        assertEquals(StatutReservation.CANCELLED, reservation.getStatutReservation());
        verify(reservationJpaRepository).save(reservation);
        assertEquals(Optional.of(reservation), reservationService.findByIdForUpdate(reservationId));
    }

    @Test
    void rejectsMissingOrNonPendingReservationOnLookupAndCancel() {
        UUID reservationId = UUID.randomUUID();
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(InvalidVDataException.class, () -> reservationService.findById(reservationId));
        assertThrows(InvalidVDataException.class, () -> reservationService.getById(reservationId));

        Reservation confirmed = Reservation.builder()
                .id(reservationId)
                .statutReservation(StatutReservation.CONFIRMED)
                .build();
        when(reservationJpaRepository.findById(reservationId)).thenReturn(Optional.of(confirmed));
        assertThrows(InvalidOperationException.class, () -> reservationService.cancel(reservationId));
        verify(reservationJpaRepository, never()).save(any(Reservation.class));
    }

    @Test
    void listsReservationsForClientAndAdminAndByEvent() {
        UUID reservationId = UUID.randomUUID();
        Client authenticatedClient = mock(Client.class);
        when(authenticatedClient.getId()).thenReturn(UUID.randomUUID());
        Reservation reservation = Reservation.builder().id(reservationId).event(event).build();
        var response = new com.eventPro.api.Dto.Reservation.ReservationResponse(
                reservationId, "REF-2", null, eventId, "eventTest",
                null, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, List.of());
        Pageable pageable = PageRequest.of(0, 3);
        Page<Reservation> page = new PageImpl<>(List.of(reservation), pageable, 1);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(clientService.findByEmail("client@mail.com")).thenReturn(authenticatedClient);
        when(reservationJpaRepository.getAllByClient(authenticatedClient, pageable)).thenReturn(page);
        when(reservationJpaRepository.findAll(pageable)).thenReturn(page);
        when(reservationJpaRepository.findAllByEventIdAndPublishedState(event)).thenReturn(List.of(reservation));
        when(reservationMapper.toResponse(reservation)).thenReturn(response);

        try (MockedStatic<UserService> mockedUser = Mockito.mockStatic(UserService.class)) {
            mockedUser.when(() -> userService.getAuthenticatedEmail(request)).thenReturn("client@mail.com");

            assertEquals(List.of(response), reservationService.getMyReservations(pageable, request).getContent());
            assertEquals(List.of(response), reservationService.getList(pageable).getContent());
            assertEquals(List.of(response), reservationService.getReservationByEventId(eventId));
        }
        verify(reservationJpaRepository).getAllByClient(authenticatedClient, pageable);
        verify(reservationJpaRepository).findAll(pageable);
        verify(reservationJpaRepository).findAllByEventIdAndPublishedState(event);
    }

}
