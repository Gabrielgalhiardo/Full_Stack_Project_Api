package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorResponseDTO;
import com.example.crud_em_aula_projeto.application.service.CollaboratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collaborators")
@RequiredArgsConstructor
@Tag(name = "Colaboradores (Admin)", description = "Endpoints para o gerenciamento de contas de colaboradores.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @PostMapping
    @Operation(summary = "Criar um novo colaborador",
            description = "Cadastra um novo colaborador no sistema com a role 'COLLABORATOR'.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Colaborador criado com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.")
            })
    public ResponseEntity<CollaboratorResponseDTO> createCollaborator(@RequestBody @Valid CollaboratorRequestDTO requestDTO) {
        CollaboratorResponseDTO newCollaborator = collaboratorService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCollaborator);
    }

    @GetMapping
    @Operation(summary = "Listar todos os colaboradores ativos",
            description = "Retorna uma lista de todos os colaboradores que estão com o status 'ativo'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.")
            })
    public ResponseEntity<List<CollaboratorResponseDTO>> getAllActiveCollaborators() {
        return ResponseEntity.ok(collaboratorService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Colaborador encontrado."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado."),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado.")
            })
    public ResponseEntity<CollaboratorResponseDTO> getCollaboratorById(@PathVariable UUID id) {
        return ResponseEntity.ok(collaboratorService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar colaborador por e-mail",
            description = "Retorna um colaborador a partir do seu endereço de e-mail.",
            parameters = {
                    @Parameter(name = "email", description = "E-mail a ser pesquisado.", required = true, example = "colaborador@email.com")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Colaborador encontrado."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado."),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado.")
            })
    public ResponseEntity<CollaboratorResponseDTO> getCollaboratorByEmail(@RequestParam String email) {
        return ResponseEntity.ok(collaboratorService.findByEmail(email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um colaborador",
            description = "Atualiza os dados (nome, e-mail, senha) de um colaborador existente.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Colaborador atualizado com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso por outro colaborador."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado."),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado.")
            })
    public ResponseEntity<CollaboratorResponseDTO> updateCollaborator(@PathVariable UUID id, @RequestBody @Valid CollaboratorRequestDTO requestDTO) {
        return ResponseEntity.ok(collaboratorService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar um colaborador (Soft Delete)",
            description = "Muda o status de um colaborador para 'inativo'.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Colaborador desativado com sucesso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado."),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado.")
            })
    public ResponseEntity<Void> deleteCollaborator(@PathVariable UUID id) {
        collaboratorService.deleteCollaborator(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reativar um colaborador",
            description = "Muda o status de um colaborador para 'ativo'.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Colaborador reativado com sucesso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado."),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado.")
            })
    public ResponseEntity<Void> activateCollaborator(@PathVariable UUID id) {
        collaboratorService.activate(id);
        return ResponseEntity.noContent().build();
    }
}