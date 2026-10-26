package com.eventPro.api.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientRegisterRequest(
        @NotBlank @Size(max = 100)
        String name ,

        @NotBlank @Email
        String email ,

        @NotBlank @Pattern(regexp = "^(\\+\\d{1,3}|0)\\d{8,12}$", message = "Numero de téléphone invalide")
        String phone ,

        @NotBlank @Size(max = 100 , min = 6)
        String password
) {}
