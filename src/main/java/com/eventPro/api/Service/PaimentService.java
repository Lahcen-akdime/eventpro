package com.eventPro.api.Service;

import com.eventPro.api.Dto.Paiment.PaimentRequest;
import com.eventPro.api.Dto.Paiment.PaimentResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Ticket;
import com.eventPro.api.Entity.TicketCategory;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Enum.TicketStatus;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Exception.AccessDeniedException;
import com.eventPro.api.Exception.IdempotencyConflictException;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.PaiementMapper;
import com.eventPro.api.Repository.PaimentJpaRepository;
import com.eventPro.api.Repository.ReservationJpaRepository;
import com.eventPro.api.Repository.TicketCategoryRepository;
import com.eventPro.api.Repository.TicketJpaRepository;
import com.eventPro.api.Utils.TicketCodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor 
public class PaimentService {

    public ReservationService reservationService ;
    private final Clock clock ;
    private final ClientService clientService ;
    private final PaimentJpaRepository paimentJpaRepository ;
    private final TicketCategoryRepository ticketCategoryRepository ;
    private final ReservationJpaRepository reservationJpaRepository ;
    private final TicketJpaRepository ticketJpaRepository ;
    private final PaiementMapper paiementMapper ;

    @Transactional
    public PaimentResponse pay(UUID reservationId, PaimentRequest request,HttpServletRequest httpRequest){

        LocalDateTime now = LocalDateTime.now(clock) ;

        Reservation reservation = reservationService.findByIdForUpdate(reservationId).orElseThrow(()->new ResourceNotFoundException("La reservation est pas existe"));

        Client client =  clientService.getAuthaunticatedClient(httpRequest);

        if(!reservation.getClient().equals(client)){
            throw new AccessDeniedException("Cette reservation est pas la tinne") ;
        }

        Paiment existingPaiment = paimentJpaRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null) ;

        if(existingPaiment != null){
                throw new IdempotencyConflictException("Cette cle d'idempotence est utilisee pour une autre demande") ;
        }

    
        if(reservation.getDateExpiration().isBefore(now)){
            throw new InvalidOperationException("La reservation est expiré") ;
        }
        if (request.amount().compareTo(reservation.getTotalAmount()) != 0) {
            throw new InvalidVDataException("Le montant ne correspond pas au total de la réservation");
        }
        Paiment paiement = Paiment.builder()
                .reference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .idempotency_key(request.idempotencyKey())
                .amount(request.amount())
                .paidAt(now)
                .statut(StatutPaiement.SUCCESS)
                .reservation(reservation)
                .build();

        List<UUID> categoryIds = reservation.getLines().stream()
                .map(line -> line.getTicketCategory().getId())
                .distinct()
                .toList();
        List<TicketCategory> locked = ticketCategoryRepository.findAllByIdInOrderByIdForUpdate(categoryIds);
        if (locked.size() != categoryIds.size()) {
            throw new IllegalStateException("Catégories introuvables pour la réservation " + reservation.getReference());
        }
        Map<UUID, TicketCategory> categories = locked.stream()
                .collect(Collectors.toMap(TicketCategory::getId, Function.identity()));
 
        reservation.confirm();
 
        List<Ticket> tickets = new ArrayList<>();
        for (LigneReservation line : reservation.getLines()) {
            long quantity = line.getQuantite();
            for (long i = 0; i < quantity; i++) {
                tickets.add(Ticket.builder()
                        .code(TicketCodeGenerator.getNewTicketCode())
                        .issuedAt(now)
                        .status(TicketStatus.VALID)
                        .reservationLine(line)
                        .ticketCategory(categories.get(line.getTicketCategory().getId()))
                        .build());
            }
        }
        List<Ticket> finalTickets = ticketJpaRepository.saveAll(tickets);
 
        reservationJpaRepository.save(reservation);
        Paiment finalSaved = paimentJpaRepository.save(paiement);
        return paiementMapper.toResponse(finalSaved,finalTickets);
    }

    public PaimentResponse getById(UUID paimentId, HttpServletRequest httpRequest){
        Client client = clientService.getAuthaunticatedClient(httpRequest);
        Paiment paiment = paimentJpaRepository.findById(paimentId).orElseThrow(() -> new ResourceNotFoundException("Paiement not fount"));
        List<Ticket> tickets = ticketJpaRepository.findAllByClientId(client.getId());
        return paiementMapper.toResponse(paiment, tickets);
    }
}
