package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Paiment;
import com.eventPro.api.Entity.Remboursement;
import com.eventPro.api.Entity.Enum.StatutPaiement;
import com.eventPro.api.Entity.Reservation.Reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaimentJpaRepository extends JpaRepository<Paiment, UUID> {

    Optional<Paiment> findByIdempotencyKey(String idempotencyKey) ;

    Optional<Paiment> findByReservationIdAndStatut(UUID reservationId, StatutPaiement success);
}
