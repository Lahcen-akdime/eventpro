package com.eventPro.api.Dto.Paiment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PaimentRequest(

        @NotBlank(message = "cle est obligatoire")
        @Size(max = 64, message = "cle ne doit pas depasser 64 caracteres")
        String idempotencyKey,

        @NotNull(message = "montant est obligatoire")
        @Positive(message = "montant doit etre positif")
        BigDecimal amount

) {}
