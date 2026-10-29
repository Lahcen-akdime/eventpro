
package com.eventPro.api.Mapper;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "ticketCategory.id", target = "ticketCategoryId")
    @Mapping(source = "ticketCategory.name", target = "ticketCategoryName")
    @Mapping(source = "ligneReservation.id", target = "reservationLineId")
    TicketResponse toResponse(Ticket ticket);
}
