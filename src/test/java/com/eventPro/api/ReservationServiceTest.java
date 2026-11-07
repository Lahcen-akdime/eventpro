package com.eventPro.api;

import com.eventPro.api.Dto.Reservation.LigneReservationRequest;
import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Exception.InsufficientStockException;
import com.eventPro.api.Mapper.ReservationMapper;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.ReservationLineJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.StrictnessSelector;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
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
    @Mock private Clock clock ;

    @InjectMocks ReservationService reservationService ;

    private UUID eventId = UUID.randomUUID() ;
    private Client client ;
    private Event event ;
    private TicketCategory ticketCategory ;


    @BeforeEach
    void init(){
       when(clock.instant()).thenReturn(NOW_INSTANT) ;
       when(clock.getZone()).thenReturn(ZoneOffset.UTC) ;

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


}
