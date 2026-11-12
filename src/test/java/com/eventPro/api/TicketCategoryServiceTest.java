package com.eventPro.api;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Mapper.TicketCategoryMapper;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Service.TicketCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TicketCategoryServiceTest {

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @Mock
    private TicketCategoryMapper ticketCategoryMapper;

    @InjectMocks
    private TicketCategoryService ticketCategoryService;

    @Test
    void createsTicketCategoryWithRequestValuesAndEvent() {
        Event event = new Event();
        TicketCategoryCreateRequest request = new TicketCategoryCreateRequest(
                "Standard", new BigDecimal("125.00"), 80, 4);

        TicketCategory result = ticketCategoryService.create(event, request);

        assertAll(
                () -> assertEquals("Standard", result.getName()),
                () -> assertEquals(new BigDecimal("125.00"), result.getUnitPrice()),
                () -> assertEquals(80, result.getTotalQuantity()),
                () -> assertEquals(4, result.getMaxPerClient()),
                () -> assertSame(event, result.getEvent()),
                () -> assertEquals(0, result.getReservedPlaces()),
                () -> assertEquals(0, result.getSoldPlaces())
        );
        verifyNoInteractions(ticketCategoryRepository, ticketCategoryMapper);
    }
}
