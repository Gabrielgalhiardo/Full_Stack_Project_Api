package com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs;

public record CollaboratorResponseDTO(
        Long id,
        String name,
        String email,
        Boolean active
) {
}
