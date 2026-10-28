package com.eventPro.api;


import com.eventPro.api.Controller.EventController;
import com.eventPro.api.Dto.Event.EventResponse;
import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Exception.EventNotFoundException;
import com.eventPro.api.Mapper.EventMapper;
import com.eventPro.api.Repository.EventJpaRepository;
import com.eventPro.api.Repository.OrganizerJpaRepository;
import com.eventPro.api.Service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventTests {

    @Mock
    private EventService eventService;

    @Mock
    private EventJpaRepository eventJpaRepository;

    @Mock
    private OrganizerJpaRepository organizerJpaRepository;

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
}