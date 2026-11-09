package com.eventPro.api.Controller.AgentControll;

import com.eventPro.api.Dto.Ticket.TicketScanRequest;
import com.eventPro.api.Dto.Ticket.TicketVerificationResponse;
import com.eventPro.api.Dto.Ticket.TicketVerifyRequest;
import com.eventPro.api.Service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events/{eventId}/tickets")
public class AgentTicketController {

    private final TicketService ticketService;

    @PostMapping("/verify")
    public ResponseEntity<TicketVerificationResponse> verify(
            @PathVariable UUID eventId,
            @Valid @RequestBody TicketVerifyRequest request) {

        return ResponseEntity.ok(
                ticketService.verify(eventId, request.code())
        );
    }

    @PostMapping("/scan")
    public ResponseEntity<TicketVerificationResponse> scan(
            @PathVariable UUID eventId,
            @Valid @RequestBody TicketScanRequest request) {

        return ResponseEntity.ok(
                ticketService.scan(eventId, request.code())
        );
    }
}
