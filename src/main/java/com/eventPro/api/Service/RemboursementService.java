package com.eventPro.api.Service;

import com.eventPro.api.Dto.Remboursement.RemboursementRequest;
import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Remboursement;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Enum.StatutRemboursement;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import com.eventPro.api.Exception.InvalidOperationException;
import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Exception.ResourceNotFoundException;
import com.eventPro.api.Mapper.RemboursementMapper;
import com.eventPro.api.Repository.PaimentJpaRepository;
import com.eventPro.api.Repository.RemboursementJpaRepository;
import com.eventPro.api.Repository.ReservationJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RemboursementService {
    private final RemboursementJpaRepository remboursementJpaRepository ;
    private final RemboursementMapper remboursementMapper ;
    private final ReservationJpaRepository reservationJpaRepository ;
    private final PaimentJpaRepository paimentJpaRepository ;
    private final ClientService clientService ;
    private final Clock clock ;

    public RemboursementResponse refund(UUID reservationId,RemboursementRequest request,HttpServletRequest httpServlet)    {
        LocalDateTime now = LocalDateTime.now(clock);
        Client client = clientService.getAuthaunticatedClient(httpServlet);
        String motif = request.motif();
 
        Optional<Reservation> reservation = reservationJpaRepository.findByIdForUpdate(reservationId);
        if(!reservation.isPresent()){
            throw new ResourceNotFoundException("Reservation not found");
        }
        if (!reservation.get().getClient().getId().equals(client.getId())) {
            throw new InvalidVDataException("reservation not found");
        }
 
        Paiment paiement = paimentJpaRepository
                .findByReservationIdAndStatut(reservationId, StatutPaiement.SUCCESS)
                .orElseThrow(() -> new InvalidOperationException("Réservation non remboursable"));
 
        Remboursement existing = remboursementJpaRepository.findByPaimentId(paiement.getId()).orElse(null);
        if (existing != null) {
            return remboursementMapper.toResponse(existing);
        }
 
        if (reservation.get().getStatutReservation() != StatutReservation.CONFIRMED) {
            throw new InvalidOperationException("Reservation non remboursable");
        }
 
            Remboursement rejected = Remboursement.builder()
                    .amount(BigDecimal.ZERO)
                    .motif(motif)
                    .requestedAt(now)
                    .paiment(paiement)
                    .build();
            rejected.refuser();
            Remboursement saved = remboursementJpaRepository.save(rejected);
            return remboursementMapper.toResponse(saved);
    }

    public RemboursementResponse getById(UUID refundId){
        return remboursementMapper.toResponse(findById(refundId)) ;
    }

    public Remboursement findById(UUID refundId){
        return remboursementJpaRepository.findById(refundId)
                                        .orElseThrow(()->new ResourceNotFoundException("Aucun reservation avec ce id")) ;
    }

    public Page<RemboursementResponse> list(Pageable pageable,HttpServletRequest httpServlet){
        Client client =  clientService.getAuthaunticatedClient(httpServlet) ;
        Page<Remboursement> remboursments =
                remboursementJpaRepository.findByPaiment_Reservation_Client_Id(client.getId(),pageable);
        return remboursments.map(remboursementMapper::toResponse);
    }

    public Page<RemboursementResponse> fullList(Pageable pageable,HttpServletRequest httpServlet){
        Page<Remboursement> remboursments = remboursementJpaRepository.findAll(pageable) ;
        return remboursments.map(remboursementMapper::toResponse);
    }
}
