package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Entity.Event;
import com.eventPro.api.Entity.Reservation.Reservation;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
    public Page<Reservation> getAllByClient(Client client, Pageable pageable) ;

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    Optional<Reservation> findByIdAndClientId(UUID id, UUID clientId);

    @Query("""
        select r.id from Reservation r
        where r.statutReservation = :statut
          and r.dateExpiration <= :now
        order by r.id
    """)
    List<UUID> findIdsToExpire(@Param("statut") StatutReservation statut,
                               @Param("now") LocalDateTime now,
                               Pageable pageable);

    @Query("""
    SELECT re from Reservation re JOIN Event ev ON re.event = :event AND ev.state = PUBLISHED 
    """)
    List<Reservation> findAllByEventIdAndPublishedState(@Param("event") Event event);

    @Query("""
    SELECT re , re.client.name , ca.name , line.quantite , ca.event
        from Reservation re JOIN TicketCategory ca JOIN LigneReservation line 
            on re.client.email = :email
              and line.reservation = re and line.ticketCategory = ca and ca.name = :nomCategorie
                and line >= :quantiteMin
    """)
    List<Reservation> findAllByClientEmailAndCategoryNameAndQuantiteMin(String email,String nomCategorie , Long quantiteMin);

}
