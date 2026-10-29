package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Ticket;
import com.eventPro.api.Entity.TicketCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT tc FROM TicketCategory tc
         WHERE tc.id IN :ids
              ORDER BY tc.id 
    """)
    List<TicketCategory> findAllByIdInOrderByIdForUpdate(List<UUID> ids) ;
}
