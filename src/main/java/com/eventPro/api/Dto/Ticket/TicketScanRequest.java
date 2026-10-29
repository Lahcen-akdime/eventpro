
package com.eventPro.api.Dto.Ticket;

import jakarta.validation.constraints.NotBlank;

public record TicketScanRequest(
        @NotBlank
        String code
) {}
