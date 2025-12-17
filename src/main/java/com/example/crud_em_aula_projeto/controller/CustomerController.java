package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.customerDTOs.CustomerRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.customerDTOs.CustomerResponseDTO;
import com.example.crud_em_aula_projeto.application.service.CustomerService;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes (User)", description = "Endpoints para o gerenciamento de contas de clientes.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "[PÚBLICO] Criar um novo cliente",
            description = """
                    Cadastra um novo cliente no sistema com a role 'USER'.
                    
                    **Requisitos:**
                    - Esta rota é pública, não requer autenticação
                    - O e-mail deve ser único no sistema
                    - A senha será criptografada automaticamente
                    - O cliente será criado com status 'ativo' por padrão
                    
                    **Validações:**
                    - Nome: obrigatório, não pode estar vazio
                    - E-mail: obrigatório, deve ser um e-mail válido e único
                    - Senha: obrigatória, não pode estar vazia
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo cliente",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "name": "Maria Silva",
                                              "email": "maria.silva@exemplo.com",
                                              "password": "senhaSegura123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cliente criado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "660e8400-e29b-41d4-a716-446655440001",
                                                      "name": "Maria Silva",
                                                      "email": "maria.silva@exemplo.com",
                                                      "active": true
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody @Valid CustomerRequestDTO requestDTO) {
        CustomerResponseDTO newCustomer = customerService.createCustomer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @GetMapping
    @Operation(
            summary = "[USER] Listar todos os clientes ativos",
            description = """
                    Retorna uma lista de todos os clientes que estão com o status 'ativo'.
                    
                    **Filtros aplicados:**
                    - Apenas clientes com `active = true` são retornados
                    - Clientes inativos não aparecem nesta lista
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de clientes ativos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<CustomerResponseDTO>> getAllActiveCustomers() {
        List<CustomerResponseDTO> customers = customerService.findAllActive();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[USER] Buscar cliente por ID",
            description = """
                    Busca um cliente específico pelo seu UUID.
                    
                    **Retorna:**
                    - Informações do cliente (id, nome, e-mail, status)
                    - Funciona tanto para clientes ativos quanto inativos
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do cliente a ser buscado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cliente encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem acessar.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {
        CustomerResponseDTO customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/search")
    @Operation(
            summary = "[USER] Buscar cliente por e-mail",
            description = """
                    Busca um cliente específico pelo seu endereço de e-mail.
                    
                    **Características:**
                    - A busca é case-insensitive (não diferencia maiúsculas/minúsculas)
                    - Retorna clientes ativos e inativos
                    - O e-mail deve ser válido e existir no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "E-mail do cliente a ser pesquisado (case-insensitive)",
                            required = true,
                            example = "cliente@email.com",
                            schema = @Schema(type = "string", format = "email")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cliente encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "E-mail inválido fornecido.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem acessar.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o e-mail fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> getCustomerByEmail(@RequestParam String email) {
        CustomerResponseDTO customer = customerService.getCustomerByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o e-mail: " + email));
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "[USER] Atualizar um cliente",
            description = """
                    Atualiza os dados de um cliente existente.
                    
                    **Campos atualizáveis:**
                    - Nome
                    - E-mail (deve ser único no sistema)
                    - Senha (será criptografada automaticamente)
                    
                    **Validações:**
                    - Todos os campos são obrigatórios
                    - E-mail deve ser válido e único
                    - Senha não pode estar vazia
                    - O cliente deve existir no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem atualizar clientes
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do cliente a ser atualizado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do cliente",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "name": "Maria Silva Atualizado",
                                              "email": "maria.silva.novo@exemplo.com",
                                              "password": "novaSenhaSegura123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cliente atualizado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já em uso por outro cliente.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem atualizar clientes.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable UUID id, @RequestBody @Valid CustomerRequestDTO requestDTO) {
        CustomerResponseDTO updatedCustomer = customerService.updateCustomer(id, requestDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "[USER] Desativar um cliente (Soft Delete)",
            description = """
                    Realiza a desativação lógica (soft delete) de um cliente.
                    
                    **Importante:**
                    - O cliente não é removido fisicamente do banco de dados
                    - O status do cliente é alterado para 'inativo' (active = false)
                    - Clientes inativos não podem fazer login no sistema
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem desativar clientes
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do cliente a ser desativado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cliente desativado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem desativar clientes.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> deactivateCustomer(@PathVariable UUID id) {
        CustomerResponseDTO deactivatedCustomer = customerService.deactivateCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return ResponseEntity.ok(deactivatedCustomer);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "[USER] Reativar um cliente",
            description = """
                    Reativa um cliente que estava inativo, mudando seu status para 'ativo'.
                    
                    **Funcionalidade:**
                    - Altera o status do cliente de `active = false` para `active = true`
                    - Após a reativação, o cliente pode fazer login novamente no sistema
                    - Todos os dados do cliente são preservados
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem reativar clientes
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do cliente a ser reativado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cliente reativado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = CustomerResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem reativar clientes.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<CustomerResponseDTO> activateCustomer(@PathVariable UUID id) {
        CustomerResponseDTO activatedCustomer = customerService.activeCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return ResponseEntity.ok(activatedCustomer);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[USER] Deletar um cliente (Hard Delete)",
            description = """
                    Remove fisicamente um cliente do banco de dados.
                    
                    **⚠️ ATENÇÃO:**
                    - Esta operação é **IRREVERSÍVEL**
                    - O cliente será **permanentemente removido** do banco de dados
                    - Todos os dados associados ao cliente serão perdidos
                    - O e-mail ficará disponível para uso em novos cadastros
                    
                    **Diferente do Soft Delete:**
                    - Soft Delete (desativar): apenas marca como inativo, dados preservados
                    - Hard Delete (deletar): remove completamente do banco
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem deletar clientes
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do cliente a ser deletado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cliente deletado permanentemente com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem deletar clientes.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }
}
