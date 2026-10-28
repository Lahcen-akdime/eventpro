package com.eventPro.api.Controller;

import com.eventPro.api.Dto.Event.EventCreateRequest;
import com.eventPro.api.Dto.Event.EventResponse;
import com.eventPro.api.Dto.Event.EventUpdateRequest;
import com.eventPro.api.Service.EventService;

import com.eventPro.api.Service.UserService;
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
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "save event")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventCreateRequest request, HttpServletRequest servletRequest) {
        return eventService.create(request, UserService.getAuthenticatedEmail(servletRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "view event")
    public EventResponse getById(@PathVariable UUID id) {
        return eventService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Events list")
    public Page<EventResponse> getAll(@PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return eventService.getAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un événement en brouillon")
    public EventResponse update(@PathVariable UUID id, @Valid @RequestBody EventUpdateRequest request) {
        return eventService.update(id, request);
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publiish event")
    public EventResponse publish(@PathVariable UUID id) {
        return eventService.publish(id);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler un événement")
    public EventResponse cancel(@PathVariable UUID id) {
        return eventService.cancel(id);
    }
}