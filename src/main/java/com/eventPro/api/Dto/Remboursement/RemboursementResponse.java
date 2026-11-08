
package com.eventPro.api.Dto.Remboursement;

import com.eventPro.api.Entity.Enum.StatutRemboursement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RemboursementResponse(

        UUID id,

        UUID reservationId,

        String reservationReference,

        BigDecimal amount,

        String motif,

        LocalDateTime requestedAt,

        StatutRemboursement statut

) {
}
