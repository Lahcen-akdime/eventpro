package com.eventPro.api.Repository;

import com.eventPro.api.Model.Paiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaimentJpaRepository extends JpaRepository<Paiment, UUID> {
}
