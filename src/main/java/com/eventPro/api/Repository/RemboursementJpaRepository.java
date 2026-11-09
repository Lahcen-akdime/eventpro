package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Remboursement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RemboursementJpaRepository extends JpaRepository<Remboursement,UUID> {

    Page<Remboursement> findByPaiment_Reservation_Client_Id(UUID clientId, Pageable pageable);

    Optional<Remboursement> findByPaimentId(UUID paimentId);


    @Query("""
            SELECT re FROM Remboursement re JOIN Paiment pa JOIN Reservation res JOIN Event ev 
                        ON ev = res.event AND pa.reservation = res AND re.paiment = pa                         
            """)
    List<Remboursement> findAllByPaimentReservationEventId(UUID eventId) ;


}
