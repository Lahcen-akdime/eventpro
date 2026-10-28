
package com.eventPro.api.Dto.Promotion;

import com.eventPro.api.Entity.Enum.TypeReduction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PromotionCreateRequest(

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{4,20}$")
        String code,

        @NotNull
        TypeReduction typeReduction,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal valeurReduction,

        @NotNull
        LocalDateTime startDate,

        @NotNull
        LocalDateTime endDate,

        @NotNull
        @Min(1)
        Long limiteUtilisation ,

        @NotNull
        UUID event_id

) {}
