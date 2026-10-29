
package com.eventPro.api.Dto.Ticket;

import jakarta.validation.constraints.NotBlank;

public record TicketVerifyRequest(
        @NotBlank
        String code
) {}
