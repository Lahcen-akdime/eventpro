package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Enum.StatutEvenement;
import com.eventPro.api.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventJpaRepository extends JpaRepository<Event, UUID> {

    List<Event> searchByState(StatutEvenement state);

}