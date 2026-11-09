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

    private final ReservationService reservationService ;
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
        Client client = clientService.getAuthaunticatedClient(httpRequest);
        Reservation reservation = reservationJpaRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("La reservation est pas existe"));

        if(!reservation.getClient().getId().equals(client.getId())){
            throw new AccessDeniedException("Cette reservation est pas la tinne") ;
        }

        Paiment existingPaiment = paimentJpaRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null) ;
        if (existingPaiment != null) {
            if (existingPaiment.getReservation().getId().equals(reservationId)
                    && existingPaiment.getAmount().compareTo(request.amount()) == 0) {
                return paiementMapper.toResponse(existingPaiment,
                        ticketJpaRepository.findAllByReservationLine_Reservation_Id(reservationId));
            }
            throw new IdempotencyConflictException("Cette cle d'idempotence est utilisee pour une autre demande") ;
        }

        if (reservation.getStatutReservation() != StatutReservation.PENDING_PAYMENT) {
            throw new InvalidOperationException("La reservation doit etre en attente de paiement");
        }
        if (reservation.isExpiredAt(now)) {
            throw new com.eventPro.api.Exception.ReservationExpiredException("La reservation est expiré");
        }
        if (request.amount().compareTo(reservation.getTotalAmount()) != 0) {
            throw new InvalidVDataException("Le montant ne correspond pas au total de la réservation");
        }

        if (request.idempotencyKey().startsWith("FAIL")) {
            Paiment failed = Paiment.builder()
                    .reference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                    .idempotencyKey(request.idempotencyKey())
                    .amount(request.amount())
                    .paidAt(now)
                    .statut(StatutPaiement.FAILED)
                    .reservation(reservation)
                    .build();
            return paiementMapper.toResponse(paimentJpaRepository.save(failed), List.of());
        }

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

        for (LigneReservation line : reservation.getLines()) {
            categories.get(line.getTicketCategory().getId()).confirmSale(line.getQuantite());
        }

        Paiment paiement = Paiment.builder()
                .reference("PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase())
                .idempotencyKey(request.idempotencyKey())
                .amount(request.amount())
                .paidAt(now)
                .statut(StatutPaiement.SUCCESS)
                .reservation(reservation)
                .build();

        reservation.confirm(now);
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
                        .client(client)
                        .build());
            }
        }
        List<Ticket> finalTickets = ticketJpaRepository.saveAll(tickets);
        Paiment finalSaved = paimentJpaRepository.save(paiement);
        reservationJpaRepository.save(reservation);
        return paiementMapper.toResponse(finalSaved, finalTickets);
    }

    public PaimentResponse getById(UUID paimentId, HttpServletRequest httpRequest){
        Client client = clientService.getAuthaunticatedClient(httpRequest);
        Paiment paiment = paimentJpaRepository.findById(paimentId).orElseThrow(() -> new ResourceNotFoundException("Paiement not fount"));
        List<Ticket> tickets = ticketJpaRepository.findAllByClientId(client.getId());
        return paiementMapper.toResponse(paiment, tickets);
    }
}
