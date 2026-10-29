package com.eventPro.api.Service;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryResponse;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Mapper.TicketCategoryMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryMapper ticketCategoryMapper ;
    private final EventService eventService ;

    public TicketCategoryResponse create(UUID eventId , TicketCategoryCreateRequest request){
        Event event = eventService.findEvent(eventId) ;
        TicketCategory ticketCategory = TicketCategory.builder().name(request.name())
                                                                .unitPrice(request.unitPrice())
                                                                .totalQuantity(request.totalQuantity())
                                                                .quotaMaxParClient(request.quotaMaxParClient())
                                                                .event(event)
                                                                .build();
        return ticketCategoryMapper.toResponse(ticketCategory) ;
    }
}
