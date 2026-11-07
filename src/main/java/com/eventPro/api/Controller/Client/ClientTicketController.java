package com.eventPro.api.Controller.Client;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Service.TicketService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/{clientId}/tickets")
@RequiredArgsConstructor
public class ClientTicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getClientTickets(
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ticketService.getAll(servletRequest));
    }
}