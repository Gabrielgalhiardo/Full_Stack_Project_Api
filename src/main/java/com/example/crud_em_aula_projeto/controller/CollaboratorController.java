package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorResponseDTO;
import com.example.crud_em_aula_projeto.application.service.CollaboratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(
            summary = "[ADMIN] Criar um novo colaborador",
            description = """
                    Cadastra um novo colaborador no sistema com a role 'COLLABORATOR'.
                    
                    **Requisitos:**
                    - Apenas administradores podem criar colaboradores
                    - O e-mail deve ser único no sistema
                    - A senha será criptografada automaticamente
                    - O colaborador será criado com status 'ativo' por padrão
                    
                    **Validações:**
                    - Nome: obrigatório, não pode estar vazio
                    - E-mail: obrigatório, deve ser um e-mail válido e único
                    - Senha: obrigatória, não pode estar vazia
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo colaborador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CollaboratorRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "name": "João Silva",
                                              "email": "joao.silva@exemplo.com",
                                              "password": "senhaSegura123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Colaborador criado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CollaboratorResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "660e8400-e29b-41d4-a716-446655440001",
                                                      "name": "João Silva",
                                                      "email": "joao.silva@exemplo.com",
                                                      "active": true
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem criar colaboradores.", content = @Content)
            }
    )
    public ResponseEntity<CollaboratorResponseDTO> createCollaborator(@RequestBody @Valid CollaboratorRequestDTO requestDTO) {
        CollaboratorResponseDTO newCollaborator = collaboratorService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCollaborator);
    }

    @GetMapping
    @Operation(
            summary = "[ADMIN] Listar todos os colaboradores ativos",
            description = """
                    Retorna uma lista de todos os colaboradores que estão com o status 'ativo'.
                    
                    **Filtros aplicados:**
                    - Apenas colaboradores com `active = true` são retornados
                    - Colaboradores inativos não aparecem nesta lista
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de colaboradores ativos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CollaboratorResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<CollaboratorResponseDTO>> getAllActiveCollaborators() {
        return ResponseEntity.ok(collaboratorService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[ADMIN] Buscar colaborador por ID",
            description = """
                    Busca um colaborador específico pelo seu UUID.
                    
                    **Retorna:**
                    - Informações do colaborador (id, nome, e-mail, status)
                    - Funciona tanto para colaboradores ativos quanto inativos
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do colaborador a ser buscado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Colaborador encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = CollaboratorResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CollaboratorResponseDTO> getCollaboratorById(@PathVariable UUID id) {
        return ResponseEntity.ok(collaboratorService.findById(id));
    }

    @GetMapping("/search")
    @Operation(
            summary = "[ADMIN] Buscar colaborador por e-mail",
            description = """
                    Busca um colaborador específico pelo seu endereço de e-mail.
                    
                    **Características:**
                    - A busca é case-insensitive (não diferencia maiúsculas/minúsculas)
                    - Retorna colaboradores ativos e inativos
                    - O e-mail deve ser válido e existir no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "E-mail do colaborador a ser pesquisado (case-insensitive)",
                            required = true,
                            example = "colaborador@email.com",
                            schema = @Schema(type = "string", format = "email")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Colaborador encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = CollaboratorResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "E-mail inválido fornecido.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado com o e-mail fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CollaboratorResponseDTO> getCollaboratorByEmail(@RequestParam String email) {
        return ResponseEntity.ok(collaboratorService.findByEmail(email));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "[ADMIN] Atualizar um colaborador",
            description = """
                    Atualiza os dados de um colaborador existente.
                    
                    **Campos atualizáveis:**
                    - Nome
                    - E-mail (deve ser único no sistema)
                    - Senha (será criptografada automaticamente)
                    
                    **Validações:**
                    - Todos os campos são obrigatórios
                    - E-mail deve ser válido e único
                    - Senha não pode estar vazia
                    - O colaborador deve existir no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem atualizar colaboradores
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do colaborador a ser atualizado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do colaborador",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CollaboratorRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "name": "João Silva Atualizado",
                                              "email": "joao.silva.novo@exemplo.com",
                                              "password": "novaSenhaSegura123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Colaborador atualizado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CollaboratorResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso por outro colaborador.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem atualizar colaboradores.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CollaboratorResponseDTO> updateCollaborator(@PathVariable UUID id, @RequestBody @Valid CollaboratorRequestDTO requestDTO) {
        return ResponseEntity.ok(collaboratorService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[ADMIN] Desativar um colaborador (Soft Delete)",
            description = """
                    Realiza a desativação lógica (soft delete) de um colaborador.
                    
                    **Importante:**
                    - O colaborador não é removido fisicamente do banco de dados
                    - O status do colaborador é alterado para 'inativo' (active = false)
                    - Colaboradores inativos não podem fazer login no sistema
                    - Produtos criados pelo colaborador permanecem no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem desativar colaboradores
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do colaborador a ser desativado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Colaborador desativado com sucesso (sem conteúdo no corpo da resposta)."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem desativar colaboradores.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteCollaborator(@PathVariable UUID id) {
        collaboratorService.deleteCollaborator(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "[ADMIN] Reativar um colaborador",
            description = """
                    Reativa um colaborador que estava inativo, mudando seu status para 'ativo'.
                    
                    **Funcionalidade:**
                    - Altera o status do colaborador de `active = false` para `active = true`
                    - Após a reativação, o colaborador pode fazer login novamente no sistema
                    - Todos os dados do colaborador são preservados
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem reativar colaboradores
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do colaborador a ser reativado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Colaborador reativado com sucesso (sem conteúdo no corpo da resposta)."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem reativar colaboradores.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Colaborador não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<Void> activateCollaborator(@PathVariable UUID id) {
        collaboratorService.activate(id);
        return ResponseEntity.noContent().build();
    }
}