package com.eventPro.api.Repository;

import com.eventPro.api.Dto.Ticket.TicketResponse;
import com.eventPro.api.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository 
public interface TicketJpaRepository extends JpaRepository<Ticket, UUID> {

    public List<Ticket> findAllByClientId(UUID clientId) ;
}
