package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Reservation.LigneReservation;
import com.eventPro.api.Entity.Reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface ReservationLineJpaRepository extends JpaRepository<LigneReservation, UUID> {

    @Query("""
            select coalesce(sum(l.quantite), 0L) from LigneReservation l
            where l.reservation.client.id = :clientId
              and l.ticketCategory.id = :categoryId
              and l.reservation.statutReservation in :statuses
            """)
    long sumActiveQuantity(@Param("clientId") UUID clientId,
                           @Param("categoryId") UUID categoryId,
                           @Param("statuses") Collection<StatutReservation> statuses);

}
