package com.eventPro.api.Controller.Organizer;

import com.eventPro.api.Dto.Organizer.OrganizerRegisterRequest;
import com.eventPro.api.Dto.Organizer.OrganizerResponse;
import com.eventPro.api.Dto.Organizer.OrganizerUpdateRequest;
import com.eventPro.api.Service.OrganizerService;

import com.eventPro.api.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizers")
@RequiredArgsConstructor
@Tag(name = "organizers")
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping
    @Operation(summary = "Inscrire un nouvel organisateur")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizerResponse save( @Valid @RequestBody OrganizerRegisterRequest request, HttpServletRequest servletRequest) {
        OrganizerResponse response = organizerService.save(request);
        HttpSession session = servletRequest.getSession();
        session.setAttribute("authEmail", response.email());
        return response;
    }

    @GetMapping("/me")
    @Operation(summary = "Consulter mon profil")
    public OrganizerResponse getMyProfile(HttpServletRequest request) {
        String email = UserService.getAuthenticatedEmail(request);
        return organizerService.getMyProfile(email);
    }

    @GetMapping
    @Operation(summary = "Lister les organisateurs avec pagination")
    public Page<OrganizerResponse> getAll(
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable) {

        return organizerService.getAll(pageable);
    }

    @PutMapping("/me")
    @Operation(summary = "Modifier mon profil")
    public OrganizerResponse updateMyProfile(@Valid @RequestBody OrganizerUpdateRequest updateRequest, HttpServletRequest request) {

        String currentEmail = UserService.getAuthenticatedEmail(request);
        return organizerService.updateMyProfile(currentEmail,updateRequest);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer le organizateur")
    public OrganizerResponse activate(@PathVariable UUID id) {
        return organizerService.activate(id) ;
    }

    @PatchMapping("/{id}/desactivate")
    @Operation(summary = "Activer un organizateur")
    public OrganizerResponse desactivate(@PathVariable UUID id) {
        return organizerService.desactivate(id) ;
    }






}