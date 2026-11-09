package com.eventPro.api.Service;

import java.util.List;

import com.eventPro.api.Dto.Ticket.TicketVerificationResponse;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Ticket;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Mapper.TicketMapper;
import com.eventPro.api.Repository.TicketJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor 
public class TicketService {

    private final TicketJpaRepository ticketJpaRepository ;
    private final TicketMapper ticketMapper ;
    private final ClientService clientService ;
    private final Clock clock;

    public List<com.eventPro.api.Dto.Ticket.TicketResponse> getAll(HttpServletRequest servletRequest) {
        Client client = clientService.getAuthaunticatedClient(servletRequest);
        return ticketJpaRepository.findAllByClientId(client.getId()).stream().map(ticketMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TicketVerificationResponse verify(UUID eventId, String code) {
        Ticket ticket = ticketJpaRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        validateEventAndReservation(ticket, eventId);
        boolean valid = ticket.getStatus() == TicketStatus.VALID;
        return toVerificationResponse(ticket, valid,
                valid ? "Ticket valide" : "Ticket deja utilise ou invalide");
    }

    @Transactional
    public TicketVerificationResponse scan(UUID eventId, String code) {
        Ticket ticket = ticketJpaRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        validateEventAndReservation(ticket, eventId);
        ticket.check(LocalDateTime.now(clock));
        return toVerificationResponse(ticket, true, "Ticket scanne avec succes");
    }

    private void validateEventAndReservation(Ticket ticket, UUID eventId) {
        if (ticket.getTicketCategory().getEvent() == null
                || !eventId.equals(ticket.getTicketCategory().getEvent().getId())) {
            throw new ResourceNotFoundException("Ticket not found for this event");
        }
        if (ticket.getReservationLine().getReservation().getStatutReservation()
                != StatutReservation.CONFIRMED) {
            throw new InvalidOperationException("La reservation du ticket n'est pas confirmee");
        }
    }

    private TicketVerificationResponse toVerificationResponse(Ticket ticket, boolean valid, String message) {
        return new TicketVerificationResponse(ticket.getId(), ticket.getCode(), ticket.getStatus(), valid, message);
    }
}
