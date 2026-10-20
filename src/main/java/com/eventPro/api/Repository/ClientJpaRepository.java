package com.eventPro.api.Repository;

import com.eventPro.api.Model.Client;
import com.eventPro.api.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientJpaRepository extends JpaRepository<Client, UUID>  {
}
