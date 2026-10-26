package com.eventPro.api.Repository;

import com.eventPro.api.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganisateurJpaRepository extends JpaRepository<User, UUID> {

}
