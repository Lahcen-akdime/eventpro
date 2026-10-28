package com.eventPro.api.Service;

import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Entity.Enum.TypeReduction;
import com.eventPro.api.Mapper.PromotionMapper;
import com.eventPro.api.Repository.PromotionJpaRepository;
import com.eventPro.api.Exception.EmailNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionJpaRepository promotionJpaRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private EventService eventService;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void testCreatePromotion() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();

        PromotionCreateRequest request = new PromotionCreateRequest(
                "fete", TypeReduction.PERCENTAGE, new BigDecimal("20.00"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 30, 23, 59),
                10L,
                eventId
        );

        PromotionResponse expectedResponse = new PromotionResponse(UUID.randomUUID(), "fete",
                TypeReduction.PERCENTAGE, new BigDecimal("20.00"), request.startDate(), request.endDate(),
                10L, 0L
        );

        when(eventService.findEvent(eventId)).thenReturn(event);
        when(promotionJpaRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionMapper.toResponse(any(Promotion.class))).thenReturn(expectedResponse);

        PromotionResponse result = promotionService.create(request);

        assertNotNull(result);
        assertEquals("fete", result.code());
        assertEquals(request.startDate(), result.startDate());
        assertEquals(request.endDate(), result.endDate());

        verify(eventService).findEvent(eventId);
        verify(promotionJpaRepository).save(any(Promotion.class));
        verify(promotionMapper).toResponse(any(Promotion.class));
    }

    @Test
    void findByIdExceptionTest() {
        UUID promotionId = UUID.randomUUID();
        when(promotionJpaRepository.findById(promotionId)).thenReturn(Optional.empty());
        assertThrows(EmailNotFoundException.class, () -> promotionService.findById(promotionId));
        verify(promotionJpaRepository).findById(promotionId);
    }
}