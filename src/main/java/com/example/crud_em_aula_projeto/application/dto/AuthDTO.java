package com.example.crud_em_aula_projeto.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTO {

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}
    public record TokenResponse(
            String token
    ) {}
}
