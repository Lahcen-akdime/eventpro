package com.eventPro.api.Controller.Client;


import com.eventPro.api.Dto.Paiment.PaimentRequest;
import com.eventPro.api.Dto.Paiment.PaimentResponse;
import com.eventPro.api.Service.PaimentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/{clientId}")
@RequiredArgsConstructor
public class ClientPaiementController {

    private final PaimentService paiementService;

    @PostMapping("/reservations/{reservationId}/payments")
    public ResponseEntity<PaimentResponse> pay(
            @PathVariable UUID reservationId,
            @Valid @RequestBody PaimentRequest request,
            HttpServletRequest httpRequest
    ) {
        PaimentResponse response = paiementService.pay( reservationId, request, httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaimentResponse> getPayment(
            @PathVariable UUID paymentId,HttpServletRequest httpRequest
    ) {
        PaimentResponse response = paiementService.getById(paymentId, httpRequest);

        return ResponseEntity.ok(response);
    }
}