package com.eventPro.api.Controller.Client;

import com.eventPro.api.Dto.Reservation.ReservationCreateRequest;
import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ClientReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "create reservation")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create( @Valid @RequestBody ReservationCreateRequest request,HttpServletRequest servletRequest) {
        return reservationService.create(request,servletRequest);
    }

    @GetMapping("/my")
    @Operation(summary = "Lister les réservations du client")
    public Page<ReservationResponse> list(@PageableDefault(size = 3) Pageable pageable, HttpServletRequest request) {
        return reservationService.getMyReservations(pageable,request ) ;
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Consulter une réservation")
    public ReservationResponse getById( @PathVariable UUID reservationId) {
        return reservationService.getById(reservationId) ;
    }


    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "Annuler une réservation en attente de paiement")
    @ResponseStatus
    public void cancel(@PathVariable UUID reservationId) {
        reservationService.cancel(reservationId);
    }
}