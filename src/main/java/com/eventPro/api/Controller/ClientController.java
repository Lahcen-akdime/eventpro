
package com.eventPro.api.Controller;

import com.eventPro.api.Dto.Client.ClientRegisterRequest;
import com.eventPro.api.Dto.Client.ClientResponse;
import com.eventPro.api.Dto.Client.ClientUpdateRequest;
import com.eventPro.api.Service.ClientService;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "clients")
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Inscrire un nouveau client")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse save(
            @Valid @RequestBody ClientRegisterRequest request,HttpServletRequest servletRequest) {
        ClientResponse clientResponse =  clientService.save(request);
        UserService.saveEmailInSession(clientResponse.email(),servletRequest);
        return clientResponse ;
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "consulter le profil")
    public ClientResponse getMyProfile(HttpServletRequest request) {
        String email = UserService.getAuthenticatedEmail(request) ;
        return clientService.getMyProfile(email);
    }

    @GetMapping
    @Operation(summary = "tous les clients")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Page<ClientResponse> getAll(
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable) {
        return clientService.getAll(pageable);
    }

    @PutMapping("/me")
    @Operation(summary = "Modifier mon profil")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ClientResponse updateMyProfile(@Valid @RequestBody ClientUpdateRequest request, HttpServletRequest servletRequest) {
        String currentEmail = UserService.getAuthenticatedEmail(servletRequest) ;
        return clientService.updateMyProfile(currentEmail,request);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer le client")
    public ClientResponse activate(@PathVariable UUID id) {
        return clientService.activate(id) ;
    }

    @PatchMapping("/{id}/desactivate")
    @Operation(summary = "Activer un client")
    public ClientResponse desactivate(@PathVariable UUID id) {
        return clientService.desactivate(id) ;
    }
}
