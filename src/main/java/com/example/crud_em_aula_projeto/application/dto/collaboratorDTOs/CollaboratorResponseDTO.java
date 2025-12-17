package com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;

import java.util.UUID;

public record CollaboratorResponseDTO(
        UUID id,
        String name,
        String email,
        Boolean active
) {
    // Construtor que recebe Entity (Entity → DTO)
    public CollaboratorResponseDTO(Collaborator collaborator) {
        this(
                collaborator.getId(),
                collaborator.getName(),
                collaborator.getEmail(),
                collaborator.getActive()
        );
    }
}
