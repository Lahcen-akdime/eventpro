
package com.eventPro.api.Controller.Organizer;

import com.eventPro.api.Dto.TicketCategory.TicketCategoryCreateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryUpdateRequest;
import com.eventPro.api.Dto.TicketCategory.TicketCategoryResponse;

import com.eventPro.api.Service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Organizer Ticket Categories")
@RequestMapping(
        "/api/v1/organizers/events/{eventId}/categories"
)
public class OrganizerTicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    // 1. Ajouter une catégorie
    @PostMapping
    public TicketCategoryResponse create( @PathVariable UUID eventId,
                                                          @Valid @RequestBody TicketCategoryCreateRequest request) {
        return ticketCategoryService.create(eventId, request);
    }

    /*// 2. Lister les catégories avec leur stock
    @GetMapping
    public ResponseEntity<List<TicketCategoryResponse>> findAll(
            @PathVariable UUID organizerId,
            @PathVariable UUID eventId) {

        return ResponseEntity.ok(
                ticketCategoryService.findAllByEvent(organizerId, eventId)
        );
    }

    // 3. Voir une catégorie
    @GetMapping("/{categoryId}")
    public ResponseEntity<TicketCategoryResponse> findById(
            @PathVariable UUID organizerId,
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId) {

        return ResponseEntity.ok(
                ticketCategoryService.findById(
                        organizerId, eventId, categoryId
                )
        );
    }

    // 4. Modifier une catégorie
    @PutMapping("/{categoryId}")
    public ResponseEntity<TicketCategoryResponse> update(
            @PathVariable UUID organizerId,
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody TicketCategoryUpdateRequest request) {

        return ResponseEntity.ok(
                ticketCategoryService.update(
                        organizerId, eventId, categoryId, request
                )
        );
    }

    // 5. Supprimer une catégorie
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID organizerId,
            @PathVariable UUID eventId,
            @PathVariable UUID categoryId) {

        ticketCategoryService.delete(
                organizerId, eventId, categoryId
        );

        return ResponseEntity.noContent().build();
    }*/
}
