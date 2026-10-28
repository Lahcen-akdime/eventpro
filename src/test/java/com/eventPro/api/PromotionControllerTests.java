package com.eventPro.api;

import com.eventPro.api.Controller.Organizer.OrganizerPromotionController;
import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Entity.Enum.TypeReduction;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Mapper.PromotionMapper;
import com.eventPro.api.Service.PromotionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PromotionControllerTests {

    @Mock
    private PromotionService promotionService;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private OrganizerPromotionController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createEndpoint() throws Exception {
        UUID eventId = UUID.randomUUID();

        PromotionCreateRequest request = new PromotionCreateRequest(
                "fete",
                TypeReduction.PERCENTAGE,
                new BigDecimal("20.00"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 30, 23, 59),
                10L,
                eventId
        );

        PromotionResponse response = new PromotionResponse(
                UUID.randomUUID(),
                "fete",
                TypeReduction.PERCENTAGE,
                new BigDecimal("20.00"),
                request.startDate(),
                request.endDate(),
                10L,
                0L
        );

        when(promotionService.create(any(PromotionCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/organizers/promotions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("fete"))
                .andExpect(jsonPath("$.nombreUtilisations").value(0));

        verify(promotionService).create(any(PromotionCreateRequest.class));
    }

    @Test
    void getByIdEndpoint() throws Exception {
        UUID promotionId = UUID.randomUUID();

        Promotion promotion = Promotion.builder()
                .id(promotionId)
                .code("fete")
                .typeReduction(TypeReduction.PERCENTAGE)
                .valeurReduction(new BigDecimal("20.00"))
                .startDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 11, 30, 23, 59))
                .limiteUtilisation(10L)
                .nombreUtilisations(0L)
                .build();

        PromotionResponse response = new PromotionResponse(
                promotionId,
                "fete",
                TypeReduction.PERCENTAGE,
                new BigDecimal("20.00"),
                promotion.getStartDate(),
                promotion.getEndDate(),
                10L,
                0L
        );

        when(promotionService.findById(promotionId)).thenReturn(promotion);
        when(promotionMapper.toResponse(promotion)).thenReturn(response);

        mockMvc.perform(get("/api/v1/organizers/promotions/{promotionId}", promotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promotionId.toString()))
                .andExpect(jsonPath("$.code").value("fete"))
                .andExpect(jsonPath("$.typeReduction").value("PERCENTAGE"));

        verify(promotionService).findById(promotionId);
        verify(promotionMapper).toResponse(promotion);
    }
}