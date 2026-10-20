package com.eventPro.api.Repository;

import com.eventPro.api.Model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public class ClientJpaRepository extends JpaRepository<Client, UUID> {
}
