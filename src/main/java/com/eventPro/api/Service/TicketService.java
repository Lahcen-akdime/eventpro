package com.eventPro.api.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Mapper.TicketMapper;
import com.eventPro.api.Repository.TicketJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class TicketService {

    private final TicketJpaRepository ticketJpaRepository ;
    private final TicketMapper ticketMapper ;
    private final ClientService clientService ;

    public List<TicketResponse> getAll(HttpServletRequest servletRequest) {
        Client client = clientService.getAuthaunticatedClient(servletRequest);
        return ticketJpaRepository.findAllByClientId(client.getId()).stream().map(ticketMapper::toResponse).toList();
    }
}
