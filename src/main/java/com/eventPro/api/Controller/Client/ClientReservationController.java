package com.eventPro.api.Controller.Client;

import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Entity.Enum.StatutReservation;
import com.eventPro.api.Service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/{clientId}/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ClientReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Créer une réservation")
    public ResponseEntity<ReservationResponse> create(
            @PathVariable UUID clientId,
            @Valid @RequestBody ReservationCreateRequest request) {

        return reservationService.create(clientId, request);

    }

    /*@GetMapping
    @Operation(summary = "Lister les réservations du client")
    public ResponseEntity<PageResponse<ReservationSummarResponse>> list(
            @PathVariable UUID clientId,
            @RequestParam(required = false) StatutReservation status,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                reservationService.list(clientId, status, pageable)
        );
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Consulter une réservation")
    public ResponseEntity<ReservationResponse> getById(
            @PathVariable UUID clientId,
            @PathVariable UUID reservationId) {

        return ResponseEntity.ok(
                reservationService.getById(clientId, reservationId)
        );
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Annuler une réservation en attente de paiement")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable UUID clientId,
            @PathVariable UUID reservationId) {

        return ResponseEntity.ok(
                reservationService.cancel(clientId, reservationId)
        );
    }*/
}