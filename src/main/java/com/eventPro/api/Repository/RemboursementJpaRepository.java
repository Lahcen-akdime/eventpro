package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Remboursement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RemboursementJpaRepository extends JpaRepository<Remboursement,UUID> {

    Page<Remboursement> findByPaiment_Reservation_Client_Id(UUID clientId, Pageable pageable);

    Optional<Remboursement> findByPaimentId(UUID paimentId);

}
