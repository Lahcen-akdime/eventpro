package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
    public Page<Reservation> getAllByClient(Client client, Pageable pageable) ;

    @Query("""
    SELECT rs FROM Reservation rs
         WHERE rs.id IN :id
    """)
    public Reservation findByIdForUpdate(UUID id) ;

}
