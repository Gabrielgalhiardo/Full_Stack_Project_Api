package com.example.crud_em_aula_projeto.application.service;


import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CollaboratorResponseDTO create(CollaboratorRequestDTO requestDTO) {
        if (usuarioRepository.findByEmail(requestDTO.email()).isPresent()) {
            throw new BusinessRuleException("E-mail já está em uso");
        }
        Collaborator newCollaborator = requestDTO.toEntity(passwordEncoder);
        collaboratorRepository.save(newCollaborator);
        return new CollaboratorResponseDTO(newCollaborator);
    }

    public CollaboratorResponseDTO findByEmail(String email) {
        Collaborator collaborator = collaboratorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with email: " + email));
        return new CollaboratorResponseDTO(collaborator);
    }

    public List<CollaboratorResponseDTO> findAllActive() {
        return collaboratorRepository.findAllByActiveTrue()
                .stream()
                .map(CollaboratorResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<CollaboratorResponseDTO> findAllOrderedByActive() {
        return collaboratorRepository.findAllOrderedByActive()
                .stream()
                .map(CollaboratorResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<CollaboratorResponseDTO> findAllInactive() {
        return collaboratorRepository.findAllByActiveFalse()
                .stream()
                .map(CollaboratorResponseDTO::new)
                .collect(Collectors.toList());
    }

    public CollaboratorResponseDTO findById(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        return new CollaboratorResponseDTO(collaborator);
    }

    public CollaboratorResponseDTO update(UUID id, CollaboratorRequestDTO requestDTO) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));

        if (!collaborator.getEmail().equals(requestDTO.email()) && usuarioRepository.findByEmail(requestDTO.email()).isPresent()) {
            throw new BusinessRuleException("E-mail já está em uso");
        }

        collaborator.setName(requestDTO.name());
        collaborator.setEmail(requestDTO.email());
        if (requestDTO.password() != null && !requestDTO.password().isEmpty()) {
            collaborator.setPasswordHash(passwordEncoder.encode(requestDTO.password()));
        }

        collaboratorRepository.save(collaborator);
        return new CollaboratorResponseDTO(collaborator);
    }

    public void deactivateCollaborator(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        collaborator.setActive(false);
        collaboratorRepository.save(collaborator);
    }

    public void deleteCollaborator(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        collaboratorRepository.delete(collaborator);
    }

    public void activate(UUID id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id: " + id));
        collaborator.setActive(true);
        collaboratorRepository.save(collaborator);
    }

}