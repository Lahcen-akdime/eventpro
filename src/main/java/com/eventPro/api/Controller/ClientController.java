package com.eventPro.api.Controller;

import com.eventPro.api.Dto.ClientRegisterRequest;
import com.eventPro.api.Dto.ClientResponse;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "clients")
public class ClientController {

    private final ClientService clientService ;

    @PostMapping
    @Operation(summary = "Inscrire un nouveau client")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse save(@Valid @RequestBody ClientRegisterRequest clientRegisterRequest){
        return clientService.save(clientRegisterRequest) ;
    }

}
