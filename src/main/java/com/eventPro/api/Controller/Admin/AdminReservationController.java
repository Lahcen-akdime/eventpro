package com.eventPro.api.Controller.Admin;

import com.eventPro.api.Dto.Reservation.ReservationResponse;
import com.eventPro.api.Service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService ;

    @GetMapping
    @Operation(summary = "Lister tous les réservations ")
    public Page<ReservationResponse> list(@PageableDefault(size = 4) Pageable pageable) {
        return reservationService.getList(pageable) ;
    }
}
