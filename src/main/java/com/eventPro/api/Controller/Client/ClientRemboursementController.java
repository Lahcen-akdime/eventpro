package com.eventPro.api.Controller.Client;

import com.eventPro.api.Dto.Remboursement.RemboursementRequest;
import com.eventPro.api.Dto.Remboursement.RemboursementResponse;
import com.eventPro.api.Entity.Enum.StatutRemboursement;
import com.eventPro.api.Service.RemboursementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Refunds")
public class ClientRemboursementController {

    private final RemboursementService remboursementService;

    // 1. Demander un remboursement
    @PostMapping("/reservations/{reservationId}/refunds")
    public ResponseEntity<RemboursementResponse> requestRefund(
            @PathVariable UUID reservationId, @Valid @RequestBody RemboursementRequest request,HttpServletRequest httpServlet
    ) {

        RemboursementResponse response =
                remboursementService.refund(reservationId, request,httpServlet);

        URI location = URI.create("/api/v1/clients/"+"/refunds/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<RemboursementResponse> getRefund(@PathVariable UUID refundId) {
        return ResponseEntity.ok(remboursementService.getById(refundId));
    }

    @GetMapping("/refunds")
    public ResponseEntity<Page<RemboursementResponse>> getMyRefunds(@RequestParam(required = false)
            @PageableDefault(
                    size = 20,
                    sort = "requestedAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable , HttpServletRequest httpServlet

    ) {

        return ResponseEntity.ok(remboursementService.list(pageable,httpServlet));
    }
}
