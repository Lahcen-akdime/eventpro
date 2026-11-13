package com.eventPro.api;

import com.eventPro.api.Dto.Promotion.PromotionCreateRequest;
import com.eventPro.api.Dto.Promotion.PromotionResponse;
import com.eventPro.api.Dto.Promotion.PromotionUpdateRequest;
import com.eventPro.api.Entity.Enum.TypeReduction;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Promotion;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.PromotionMapper;
import com.eventPro.api.Repository.PromotionJpaRepository;
import com.eventPro.api.Service.EventService;
import com.eventPro.api.Service.PromotionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    // ---------- create ----------

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

    // ---------- getList ----------

    @Test
    void getListTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Promotion promotion = mock(Promotion.class);
        PromotionResponse response = mock(PromotionResponse.class);
        when(promotionJpaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(promotion)));
        when(promotionMapper.toResponse(promotion)).thenReturn(response);

        Page<PromotionResponse> result = promotionService.getList(pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));
    }

    // ---------- findById ----------

    @Test
    void findByIdSuccessTest() {
        UUID promotionId = UUID.randomUUID();
        Promotion promotion = mock(Promotion.class);
        when(promotionJpaRepository.findById(promotionId)).thenReturn(Optional.of(promotion));

        assertSame(promotion, promotionService.findById(promotionId));
    }

    @Test
    void findByIdExceptionTest() {
        UUID promotionId = UUID.randomUUID();
        when(promotionJpaRepository.findById(promotionId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> promotionService.findById(promotionId));
        verify(promotionJpaRepository).findById(promotionId);
    }

    // ---------- findByCode ----------

    @Test
    void findByCodeSuccessTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotionJpaRepository.findByCode("fete")).thenReturn(Optional.of(promotion));

        assertSame(promotion, promotionService.findByCode("fete"));
    }

    @Test
    void findByCodeNotFoundTest() {
        when(promotionJpaRepository.findByCode("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promotionService.findByCode("inconnu"));
    }

    // ---------- findApplicablePromotion ----------

    @Test
    void findApplicablePromotionSuccessTest() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 12, 0);
        Event event = new Event();
        Promotion promotion = mock(Promotion.class);
        when(promotionJpaRepository.findByCode("fete")).thenReturn(Optional.of(promotion));
        when(promotion.isValidAt(now)).thenReturn(true);
        when(promotion.isApplicableTo(event)).thenReturn(true);

        assertSame(promotion, promotionService.findApplicablePromotion("fete", now, event));
    }

    @Test
    void findApplicablePromotionCodeNotFoundTest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event();
        when(promotionJpaRepository.findByCode("inconnu")).thenReturn(Optional.empty());

        assertThrows(InvalidOperationException.class,
                () -> promotionService.findApplicablePromotion("inconnu", now, event));
    }

    @Test
    void findApplicablePromotionNotValidAtTest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event();
        Promotion promotion = mock(Promotion.class);
        when(promotionJpaRepository.findByCode("fete")).thenReturn(Optional.of(promotion));
        when(promotion.isValidAt(now)).thenReturn(false);

        assertThrows(InvalidOperationException.class,
                () -> promotionService.findApplicablePromotion("fete", now, event));
    }

    @Test
    void findApplicablePromotionNotApplicableToEventTest() {
        LocalDateTime now = LocalDateTime.now();
        Event event = new Event();
        Promotion promotion = mock(Promotion.class);
        when(promotionJpaRepository.findByCode("fete")).thenReturn(Optional.of(promotion));
        when(promotion.isValidAt(now)).thenReturn(true);
        when(promotion.isApplicableTo(event)).thenReturn(false);

        assertThrows(InvalidOperationException.class,
                () -> promotionService.findApplicablePromotion("fete", now, event));
    }

    // ---------- update ----------

    @Test
    void updateSuccessTest() {
        UUID id = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 12, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);
        BigDecimal valeur = new BigDecimal("15.00");

        PromotionUpdateRequest request = mock(PromotionUpdateRequest.class);
        when(request.startDate()).thenReturn(start);
        when(request.endDate()).thenReturn(end);
        when(request.limiteUtilisation()).thenReturn(50L);
        when(request.valeurReduction()).thenReturn(valeur);

        Promotion promotion = mock(Promotion.class);
        PromotionResponse response = mock(PromotionResponse.class);
        when(promotionJpaRepository.findById(id)).thenReturn(Optional.of(promotion));
        when(promotionMapper.toResponse(promotion)).thenReturn(response);

        PromotionResponse result = promotionService.update(id, request);

        assertSame(response, result);
        verify(promotion).setStartDate(start);
        verify(promotion).setEndDate(end);
        verify(promotion).setLimiteUtilisation(50L);
        verify(promotion).setValeurReduction(valeur);
        verify(promotionJpaRepository).save(promotion);
    }

    @Test
    void updateNotFoundTest() {
        UUID id = UUID.randomUUID();
        PromotionUpdateRequest request = mock(PromotionUpdateRequest.class);
        when(promotionJpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> promotionService.update(id, request));
        verify(promotionJpaRepository, never()).save(any());
    }

    // ---------- checkIsActive ----------

    @Test
    void checkIsActiveOkTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getActive()).thenReturn(true);

        assertDoesNotThrow(() -> promotionService.checkIsActive(promotion));
    }

    @Test
    void checkIsActiveInactiveTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getActive()).thenReturn(false);

        assertThrows(InvalidOperationException.class, () -> promotionService.checkIsActive(promotion));
    }

    // ---------- checkIsPossibleNow ----------

    @Test
    void checkIsPossibleNowOkTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getStartDate()).thenReturn(LocalDateTime.now().minusDays(1));
        when(promotion.getEndDate()).thenReturn(LocalDateTime.now().plusDays(1));

        assertDoesNotThrow(() -> promotionService.checkIsPossibleNow(promotion));
    }

    @Test
    void checkIsPossibleNowNotStartedTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getStartDate()).thenReturn(LocalDateTime.now().plusDays(1));

        assertThrows(InvalidOperationException.class, () -> promotionService.checkIsPossibleNow(promotion));
    }

    @Test
    void checkIsPossibleNowExpiredTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getStartDate()).thenReturn(LocalDateTime.now().minusDays(10));
        when(promotion.getEndDate()).thenReturn(LocalDateTime.now().minusDays(1));

        assertThrows(InvalidOperationException.class, () -> promotionService.checkIsPossibleNow(promotion));
    }

    // ---------- AddNewNumberOfUtilisation ----------

    @Test
    void addNewNumberOfUtilisationWithinLimitTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getNombreUtilisations()).thenReturn(4L);
        when(promotion.getLimiteUtilisation()).thenReturn(10L);

        assertDoesNotThrow(() -> promotionService.AddNewNumberOfUtilisation(promotion, 3L));

        verify(promotion).addUtilisation(3L);
    }

    @Test
    void addNewNumberOfUtilisationExceedsLimitTest() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.getNombreUtilisations()).thenReturn(8L);
        when(promotion.getLimiteUtilisation()).thenReturn(10L);

        assertThrows(InvalidOperationException.class,
                () -> promotionService.AddNewNumberOfUtilisation(promotion, 3L));

        verify(promotion, never()).addUtilisation(any());
    }

}