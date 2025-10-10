package com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CollaboratorRequestDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
