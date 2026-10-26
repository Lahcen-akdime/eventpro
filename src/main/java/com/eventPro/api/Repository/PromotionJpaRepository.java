package com.eventPro.api.Repository;

import com.eventPro.api.Entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PromotionJpaRepository extends JpaRepository<Promotion, UUID> {
}
