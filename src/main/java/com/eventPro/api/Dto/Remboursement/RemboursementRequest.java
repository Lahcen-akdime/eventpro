
package com.eventPro.api.Dto.Remboursement;

import jakarta.validation.constraints.Size;

public record RemboursementRequest(

        @Size(max = 255, message = "Le motif ne doit pas depasser 255 au max")
        String motif

) {
}
