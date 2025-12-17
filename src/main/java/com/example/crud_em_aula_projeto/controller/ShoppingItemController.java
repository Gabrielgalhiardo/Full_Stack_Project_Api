package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingItemRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingItemResponseDTO;
import com.example.crud_em_aula_projeto.application.service.ShoppingItemService;
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
@RequestMapping("/api/shopping-items")
@RequiredArgsConstructor
@Tag(name = "Itens do Carrinho (User)", description = "Endpoints para o gerenciamento de itens do carrinho de compras.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    @GetMapping("/my-items")
    @Operation(
            summary = "[USER] Obter meus itens do carrinho",
            description = """
                    Retorna todos os itens do carrinho de compras do cliente autenticado.
                    
                    **Informações retornadas:**
                    - Lista de itens com detalhes do produto
                    - Quantidade de cada item
                    - Subtotal de cada item
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Retorna apenas os itens do próprio cliente
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de itens retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingItemResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "id": "770e8400-e29b-41d4-a716-446655440002",
                                                        "productId": "880e8400-e29b-41d4-a716-446655440003",
                                                        "productTitle": "Produto Exemplo",
                                                        "productPrice": 99.99,
                                                        "quantity": 2,
                                                        "subTotal": 199.98
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<List<ShoppingItemResponseDTO>> getMyItems() {
        List<ShoppingItemResponseDTO> items = shoppingItemService.getItemsByAuthenticatedCustomer();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[USER] Obter item por ID",
            description = """
                    Retorna um item específico do carrinho pelo seu UUID.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Apenas o dono do item pode acessá-lo
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do item",
                            required = true,
                            example = "770e8400-e29b-41d4-a716-446655440002"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Item encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingItemResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para acessar este item.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingItemResponseDTO> getItemById(@PathVariable UUID id) {
        ShoppingItemResponseDTO item = shoppingItemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/shopping/{shoppingId}")
    @Operation(
            summary = "[USER] Obter itens por carrinho",
            description = """
                    Retorna todos os itens de um carrinho específico.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Apenas o dono do carrinho pode acessar seus itens
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "shoppingId",
                            description = "UUID do carrinho de compras",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de itens retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingItemResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para acessar este carrinho.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<List<ShoppingItemResponseDTO>> getItemsByShoppingId(@PathVariable UUID shoppingId) {
        List<ShoppingItemResponseDTO> items = shoppingItemService.getItemsByShoppingId(shoppingId);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @Operation(
            summary = "[USER] Adicionar item ao carrinho",
            description = """
                    Adiciona um novo item ao carrinho de compras do cliente autenticado.
                    
                    **Comportamento:**
                    - Se o produto já estiver no carrinho, a quantidade será incrementada
                    - Se o produto não estiver no carrinho, um novo item será criado
                    - O carrinho será criado automaticamente se não existir
                    
                    **Validações:**
                    - O produto deve existir
                    - O produto deve estar disponível (status AVAILABLE)
                    - A quantidade deve ser maior que zero
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem adicionar itens
                    - Requer autenticação JWT
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do item a ser adicionado",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ShoppingItemRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "productId": "880e8400-e29b-41d4-a716-446655440003",
                                              "quantity": 2
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Item adicionado ao carrinho com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingItemResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou produto não disponível.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingItemResponseDTO> addItem(@RequestBody @Valid ShoppingItemRequestDTO requestDTO) {
        ShoppingItemResponseDTO item = shoppingItemService.createItem(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "[USER] Atualizar quantidade do item",
            description = """
                    Atualiza a quantidade de um item específico no carrinho.
                    
                    **Validações:**
                    - A quantidade deve ser maior que zero
                    - Apenas o dono do item pode atualizá-lo
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem atualizar itens
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do item a ser atualizado",
                            required = true,
                            example = "770e8400-e29b-41d4-a716-446655440002"
                    ),
                    @Parameter(
                            name = "quantity",
                            description = "Nova quantidade do item",
                            required = true,
                            example = "3"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Item atualizado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingItemResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Quantidade inválida (deve ser maior que zero).", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para modificar este item.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingItemResponseDTO> updateItem(
            @PathVariable UUID id,
            @RequestParam @Parameter(description = "Nova quantidade") Integer quantity) {
        ShoppingItemResponseDTO updatedItem = shoppingItemService.updateItem(id, quantity);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[USER] Remover item do carrinho",
            description = """
                    Remove um item específico do carrinho de compras.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem remover itens
                    - Apenas o dono do item pode removê-lo
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do item a ser removido",
                            required = true,
                            example = "770e8400-e29b-41d4-a716-446655440002"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Item removido com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para remover este item.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        shoppingItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/my-items/clear")
    @Operation(
            summary = "[USER] Limpar todos os meus itens",
            description = """
                    Remove todos os itens do carrinho de compras do cliente autenticado.
                    
                    **Importante:**
                    - Remove todos os itens do carrinho
                    - O carrinho permanece, apenas fica vazio
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem limpar seus itens
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Todos os itens foram removidos com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> clearMyItems() {
        shoppingItemService.deleteAllItemsByAuthenticatedCustomer();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shopping/{shoppingId}/clear")
    @Operation(
            summary = "[USER] Limpar itens de um carrinho específico",
            description = """
                    Remove todos os itens de um carrinho específico.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem limpar itens
                    - Apenas o dono do carrinho pode limpar seus itens
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "shoppingId",
                            description = "UUID do carrinho",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Todos os itens foram removidos com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para limpar este carrinho.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> clearItemsByShoppingId(@PathVariable UUID shoppingId) {
        shoppingItemService.deleteAllItemsByShoppingId(shoppingId);
        return ResponseEntity.noContent().build();
    }
}

