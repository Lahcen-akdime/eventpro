package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryResponse;
import com.eventPro.api.Entity.TicketCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketCategoryMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "availblePlaces", target = "availablePlaces")
    TicketCategoryResponse toResponse(TicketCategory category);
}
