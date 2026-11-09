package com.eventPro.api.Service;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryResponse;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Mapper.TicketCategoryMapper;
import com.eventPro.api.Repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository ;
    private final TicketCategoryMapper ticketCategoryMapper ;


    @Transactional
    public TicketCategory create(Event event , TicketCategoryCreateRequest request){
        TicketCategory ticketCategory = TicketCategory.builder().name(request.name())
                                                                .unitPrice(request.unitPrice())
                                                                .totalQuantity(request.totalQuantity())
                                                                .maxPerClient(request.MaxPerClient())
                                                                .event(event)
                                                                .build();

        return ticketCategory ;
    }

}
