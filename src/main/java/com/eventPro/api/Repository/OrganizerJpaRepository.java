package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerJpaRepository extends JpaRepository<Organizer, UUID> {

    Optional<Organizer> findByEmailIgnoreCase(String email);

}
