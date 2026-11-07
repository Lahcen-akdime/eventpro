package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketResponse toResponse(Ticket ticket);
}