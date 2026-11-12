package com.eventPro.api;

import com.eventPro.api.Controller.EventController;
import com.eventPro.api.Dto.Event.EventCreateRequest;
import com.eventPro.api.Dto.Event.EventResponse;
import com.eventPro.api.Dto.Event.EventUpdateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Organizer;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Exception.EmailNotFoundException;
import com.eventPro.api.Exception.EventNotFoundException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Mapper.EventMapper;
import com.eventPro.api.Repository.EventJpaRepository;
import com.eventPro.api.Repository.OrganizerJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.EventService;
import com.eventPro.api.Service.TicketCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventTests {

    @Mock
    private EventService eventService;

    @Mock
    private EventJpaRepository eventJpaRepository;

    @Mock
    private OrganizerJpaRepository organizerJpaRepository;

    @Mock
    private TicketCategoryService ticketCategoryService;

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventController eventController;

    @InjectMocks
    private EventService eventServiceImpl;

    private MockMvc mockMvc;

    private UUID eventId;
    private Event event;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private EventResponse eventResponse;

    @BeforeEach
    void fakeData() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();

        eventId = UUID.randomUUID();

        startDate = LocalDateTime.of(2027, 5, 15, 9, 0);
        endDate = LocalDateTime.of(2027, 5, 15, 18, 0);
        openDate = LocalDateTime.of(2027, 1, 1, 9, 0);
        closeDate = LocalDateTime.of(2027, 5, 15, 8, 0);

        event = Event.builder().id(eventId).title("CME1").description("Linkedin").place("Safi")
                .startDate(startDate).endDate(endDate).openDate(openDate).closeDate(closeDate)
                .state(StatutEvenement.DRAFT).build();

        eventResponse = EventResponse.builder().id(eventId).title("CME1")
                .description("Linkedin").place("Safi").startDate(startDate)
                .endDate(endDate).openDate(openDate).closeDate(closeDate)
                .state(StatutEvenement.DRAFT).build();
    }


    @Test
    void ReturnEventById() throws Exception {
        when(eventService.getById(eventId))
                .thenReturn(eventResponse);

        mockMvc.perform(get("/api/v1/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("CME1"))
                .andExpect(jsonPath("$.state")
                        .value("DRAFT"));

        verify(eventService).getById(eventId);
    }

    @Test
    void publishEventTest() throws Exception {
        EventResponse publishedResponse = EventResponse.builder().id(eventId).title("CME").state(StatutEvenement.PUBLISHED)
                .build();

        when(eventService.publish(eventId))
                .thenReturn(publishedResponse);

        mockMvc.perform(patch(
                        "/api/v1/events/{id}/publish", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state")
                        .value("PUBLISHED"));

        verify(eventService).publish(eventId);
    }


    @Test
    void getByIdTest() {
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);
        EventResponse result = eventServiceImpl.getById(eventId);
        assertNotNull(result);
        assertEquals(eventId, result.id());
        assertEquals("CME1", result.title());
        verify(eventJpaRepository).findById(eventId);
        verify(eventMapper).toResponse(event);
    }

    @Test
    void eventIdNotExistTest() {
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.getById(eventId));
        verify(eventMapper, never()).toResponse(any());
    }


    @Test
    void getAllTest() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        Page<EventResponse> result = eventServiceImpl.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(eventResponse, result.getContent().get(0));
    }


    @Test
    void createSuccessTest() {
        String email = "organizer@gmail.com";
        EventCreateRequest request = mock(EventCreateRequest.class);
        TicketCategoryCreateRequest ticketRequest = mock(TicketCategoryCreateRequest.class);
        Organizer organizer = mock(Organizer.class);
        TicketCategory ticket = mock(TicketCategory.class);

        when(request.startDate()).thenReturn(startDate);
        when(request.endDate()).thenReturn(endDate);
        when(request.openDate()).thenReturn(openDate);
        when(request.closeDate()).thenReturn(closeDate);
        when(request.categories()).thenReturn(List.of(ticketRequest));
        when(organizerJpaRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(request)).thenReturn(event);
        when(ticketCategoryService.create(event, ticketRequest)).thenReturn(ticket);
        when(eventJpaRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventServiceImpl.create(request, email);

        assertSame(eventResponse, result);
        assertSame(organizer, event.getOrganizer());
        assertEquals(StatutEvenement.DRAFT, event.getState());
        assertEquals(List.of(ticket), event.getTickets());
        verify(ticketCategoryRepository).saveAll(List.of(ticket));
    }

    @Test
    void createOrganizerNotFoundTest() {
        String email = "inconnu@gmail.com";
        EventCreateRequest request = mock(EventCreateRequest.class);
        when(request.startDate()).thenReturn(startDate);
        when(request.endDate()).thenReturn(endDate);
        when(request.openDate()).thenReturn(openDate);
        when(request.closeDate()).thenReturn(closeDate);
        when(organizerJpaRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> eventServiceImpl.create(request, email));
        verify(eventJpaRepository, never()).save(any());
        verify(ticketCategoryRepository, never()).saveAll(any());
    }

    @Test
    void createRejectsEventWhenStartDateIsNotBeforeEndDate() {
        EventCreateRequest request = mock(EventCreateRequest.class);
        when(request.startDate()).thenReturn(endDate);
        when(request.endDate()).thenReturn(startDate);
        when(request.openDate()).thenReturn(openDate);
        when(request.closeDate()).thenReturn(closeDate);

        assertThrows(InvalidVDataException.class, () -> eventServiceImpl.create(request, "organizer@gmail.com"));

        verifyNoInteractions(organizerJpaRepository, eventMapper, ticketCategoryService,
                eventJpaRepository, ticketCategoryRepository);
    }

    @Test
    void createRejectsEventWhenTicketSalesDatesAreInvalid() {
        EventCreateRequest request = mock(EventCreateRequest.class);
        when(request.startDate()).thenReturn(startDate);
        when(request.endDate()).thenReturn(endDate);
        when(request.openDate()).thenReturn(closeDate);
        when(request.closeDate()).thenReturn(openDate);

        assertThrows(InvalidVDataException.class, () -> eventServiceImpl.create(request, "organizer@gmail.com"));

        verifyNoInteractions(organizerJpaRepository, eventMapper, ticketCategoryService,
                eventJpaRepository, ticketCategoryRepository);
   }


    @Test
    void updateSuccessTest() {
        EventUpdateRequest request = mock(EventUpdateRequest.class);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventJpaRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventServiceImpl.update(eventId, request);

        assertSame(eventResponse, result);
        verify(eventMapper).updateEntity(request, event);
        verify(eventJpaRepository).save(event);
    }

    @Test
    void updateNotDraftTest() {
        EventUpdateRequest request = mock(EventUpdateRequest.class);
        event.setState(StatutEvenement.PUBLISHED);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ResponseStatusException.class, () -> eventServiceImpl.update(eventId, request));
        verify(eventMapper, never()).updateEntity(any(), any());
        verify(eventJpaRepository, never()).save(any());
    }

    @Test
    void updateEventNotFoundTest() {
        EventUpdateRequest request = mock(EventUpdateRequest.class);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventServiceImpl.update(eventId, request));
        verify(eventJpaRepository, never()).save(any());
    }

    @Test
    void updateRejectsEventWithInvalidStoredDates() {
        event.setStartDate(endDate);
        event.setEndDate(startDate);
        EventUpdateRequest request = mock(EventUpdateRequest.class);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(InvalidVDataException.class, () -> eventServiceImpl.update(eventId, request));

        verify(eventMapper, never()).updateEntity(any(), any());
        verify(eventJpaRepository, never()).save(any());
    }


    @Test
    void publishSuccessTest() {
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventJpaRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventServiceImpl.publish(eventId);

        assertSame(eventResponse, result);
        assertEquals(StatutEvenement.PUBLISHED, event.getState());
        verify(eventJpaRepository).save(event);
    }

    @Test
    void publishNotDraftTest() {
        event.setState(StatutEvenement.PUBLISHED);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(InvalidVDataException.class, () -> eventServiceImpl.publish(eventId));
        verify(eventJpaRepository, never()).save(any());
    }


    @Test
    void cancelSuccessTest() {
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventJpaRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventServiceImpl.cancel(eventId);

        assertSame(eventResponse, result);
        assertEquals(StatutEvenement.CANCELLED, event.getState());
        verify(eventJpaRepository).save(event);
    }

    @Test
    void cancelAlreadyCancelledTest() {
        event.setState(StatutEvenement.CANCELLED);
        when(eventJpaRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(InvalidVDataException.class, () -> eventServiceImpl.cancel(eventId));
        verify(eventJpaRepository, never()).save(any());
    }
}