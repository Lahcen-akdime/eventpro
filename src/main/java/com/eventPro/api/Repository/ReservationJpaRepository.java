package com.eventPro.api.Repository;

import com.eventPro.api.Model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationJpaRepository extends JpaRepository<Reservation, UUID> {
}
