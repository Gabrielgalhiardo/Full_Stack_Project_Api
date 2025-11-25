package com.example.crud_em_aula_projeto.application.service;


import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final PasswordEncoder passwordEncoder;

    public CollaboratorResponseDTO create(CollaboratorRequestDTO requestDTO) {
        if (collaboratorRepository.findByEmail(requestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        Collaborator newCollaborator = Collaborator.builder()
                .name(requestDTO.name())
                .email(requestDTO.email())
                .passwordHash(passwordEncoder.encode(requestDTO.password()))
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        collaboratorRepository.save(newCollaborator);

        return new CollaboratorResponseDTO(
                newCollaborator.getId(),
                newCollaborator.getName(),
                newCollaborator.getEmail(),
                newCollaborator.getActive()
        );
    }

    public CollaboratorResponseDTO findByEmail(String email) {
        Collaborator collaborator = collaboratorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with email: " + email));
        return new CollaboratorResponseDTO(collaborator.getId(), collaborator.getName(), collaborator.getEmail(), collaborator.getActive());
    }

    public List<CollaboratorResponseDTO> findAllActive() {
        return collaboratorRepository.findAllByActiveTrue()
                .stream()
                .map(c -> new CollaboratorResponseDTO(c.getId(), c.getName(), c.getEmail(), c.getActive()))
                .collect(Collectors.toList());
    }

    public CollaboratorResponseDTO findById(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        return new CollaboratorResponseDTO(collaborator.getId(), collaborator.getName(), collaborator.getEmail(), collaborator.getActive());
    }

    public CollaboratorResponseDTO update(UUID id, CollaboratorRequestDTO requestDTO) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));

        if (!collaborator.getEmail().equals(requestDTO.email()) && collaboratorRepository.findByEmail(requestDTO.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        collaborator.setName(requestDTO.name());
        collaborator.setEmail(requestDTO.email());
        if (requestDTO.password() != null && !requestDTO.password().isEmpty()) {
            collaborator.setPasswordHash(passwordEncoder.encode(requestDTO.password()));
        }

        collaboratorRepository.save(collaborator);

        return new CollaboratorResponseDTO(collaborator.getId(), collaborator.getName(), collaborator.getEmail(), collaborator.getActive());
    }

    public void deleteCollaborator(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        collaborator.setActive(false);
        collaboratorRepository.save(collaborator);
    }

    public void activate(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        collaborator.setActive(true);
        collaboratorRepository.save(collaborator);
    }

}