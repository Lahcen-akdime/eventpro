package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryResponse;
import com.eventPro.api.Entity.TicketCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketCategoryMapper {

    //@Mapping(source = "event.id", target = "event_id")
    TicketCategoryResponse toResponse(TicketCategory category);

    TicketCategory toEntity(TicketCategoryCreateRequest ticketCategoryCreateRequest) ;
}
