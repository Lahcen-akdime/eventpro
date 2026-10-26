package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketCategoryRepository extends JpaRepository<Ticket, UUID> {
}
