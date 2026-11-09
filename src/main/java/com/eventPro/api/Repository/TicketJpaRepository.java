package com.eventPro.api.Repository;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository 
public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {

    public List<Ticket> findAllByClientId(UUID clientId) ;

    List<Ticket> findAllByReservationLine_Reservation_Id(UUID reservationId);

    Optional<Ticket> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.code = :code")
    Optional<Ticket> findByCodeForUpdate(@Param("code") String code);
}
