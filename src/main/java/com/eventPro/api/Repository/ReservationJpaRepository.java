package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Client;
import com.eventPro.api.Entity.Reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
    public Page<Reservation> getAllByClient(Client client, Pageable pageable) ;

}
