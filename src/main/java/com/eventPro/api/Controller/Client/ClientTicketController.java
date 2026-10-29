
package com.eventPro.api.Controller.Client;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Enum.TicketStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ClientTicketController {

    /*private final TicketService ticketService;

    // 1. Mes billets
    @GetMapping("/api/v1/clients/{clientId}/tickets")
    public ResponseEntity<PageResponse<TicketResponse>> findMyTickets(
            @PathVariable UUID clientId,
            @RequestParam(required = false) TicketStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "dateEmission",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                ticketService.findByClient(clientId, status, pageable)
        );
    }

    // 2. Billets d'une réservation
    @GetMapping(
            "/api/v1/clients/{clientId}/reservations/{reservationId}/tickets"
    )
    public ResponseEntity<List<TicketResponse>> findReservationTickets(
            @PathVariable UUID clientId,
            @PathVariable UUID reservationId) {

        return ResponseEntity.ok(
                ticketService.findByReservation(clientId, reservationId)
        );
    }*/
}
