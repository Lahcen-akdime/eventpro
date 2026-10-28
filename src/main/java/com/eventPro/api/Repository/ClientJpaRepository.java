package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientJpaRepository extends JpaRepository<Client, UUID>  {
    Optional<Client> findByEmail(String email);
}
