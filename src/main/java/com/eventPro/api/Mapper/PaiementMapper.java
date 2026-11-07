package com.eventPro.api.Mapper;


import com.eventPro.api.Dto.Paiment.PaimentResponse;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = TicketMapper.class)
public interface PaiementMapper {

    @Mapping(source = "tickets", target = "tickets")
    PaimentResponse toResponse(Paiment paiment, List<Ticket> tickets);
}