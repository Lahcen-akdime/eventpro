
package com.eventPro.api.Dto.Promotion;

import com.eventPro.api.Entity.Enum.TypeReduction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String code,
        TypeReduction typeReduction,
        BigDecimal valeurReduction,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long limiteUtilisation,
        Long nombreUtilisations
) {}
