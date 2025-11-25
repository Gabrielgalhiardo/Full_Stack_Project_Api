package com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs;

import java.util.UUID;

public record CollaboratorResponseDTO(
        UUID id,
        String name,
        String email,
        Boolean active
) {
}
