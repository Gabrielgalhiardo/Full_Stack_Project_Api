package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingResponseDTO;
import com.example.crud_em_aula_projeto.application.service.ShoppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shopping")
@RequiredArgsConstructor
@Tag(name = "Carrinho de Compras (User)", description = "Endpoints para o gerenciamento de carrinhos de compras.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class ShoppingController {

    private final ShoppingService shoppingService;

    @GetMapping("/my-cart")
    @Operation(
            summary = "[USER] Obter meu carrinho de compras",
            description = """
                    Retorna o carrinho de compras do cliente autenticado.
                    
                    **Informações retornadas:**
                    - ID do carrinho
                    - ID do cliente
                    - Lista de itens no carrinho
                    - Valor total do carrinho
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Retorna apenas o carrinho do próprio cliente
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Carrinho de compras retornado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "660e8400-e29b-41d4-a716-446655440001",
                                                      "customerId": "550e8400-e29b-41d4-a716-446655440000",
                                                      "items": [
                                                        {
                                                          "id": "770e8400-e29b-41d4-a716-446655440002",
                                                          "productId": "880e8400-e29b-41d4-a716-446655440003",
                                                          "productTitle": "Produto Exemplo",
                                                          "productPrice": 99.99,
                                                          "quantity": 2,
                                                          "subTotal": 199.98
                                                        }
                                                      ],
                                                      "totalAmount": 199.98
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas usuários com role USER podem acessar.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado para este cliente.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingResponseDTO> getMyShopping() {
        ShoppingResponseDTO shopping = shoppingService.getShoppingByAuthenticatedCustomer();
        return ResponseEntity.ok(shopping);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[USER] Obter carrinho por ID",
            description = """
                    Retorna um carrinho de compras específico pelo seu UUID.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do carrinho de compras",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Carrinho de compras encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado com o ID fornecido.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingResponseDTO> getShoppingById(@PathVariable UUID id) {
        ShoppingResponseDTO shopping = shoppingService.getShoppingById(id);
        return ResponseEntity.ok(shopping);
    }

    @PostMapping
    @Operation(
            summary = "[USER] Criar um novo carrinho de compras",
            description = """
                    Cria um novo carrinho de compras para o cliente autenticado.
                    
                    **Importante:**
                    - Cada cliente pode ter apenas um carrinho de compras
                    - Se o cliente já possuir um carrinho, retornará erro
                    - O carrinho será criado vazio inicialmente
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem criar carrinhos
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Carrinho de compras criado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Cliente já possui um carrinho de compras.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
            }
    )
    public ResponseEntity<ShoppingResponseDTO> createShopping() {
        ShoppingResponseDTO newShopping = shoppingService.createShopping();
        return ResponseEntity.status(HttpStatus.CREATED).body(newShopping);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[USER] Deletar carrinho de compras",
            description = """
                    Remove permanentemente um carrinho de compras.
                    
                    **⚠️ ATENÇÃO:**
                    - Esta operação é **IRREVERSÍVEL**
                    - Todos os itens do carrinho serão removidos
                    - Apenas o dono do carrinho pode deletá-lo
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem deletar carrinhos
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do carrinho a ser deletado",
                            required = true,
                            example = "660e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Carrinho deletado com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para deletar este carrinho.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteShopping(@PathVariable UUID id) {
        shoppingService.deleteShopping(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/my-cart/clear")
    @Operation(
            summary = "[USER] Limpar meu carrinho de compras",
            description = """
                    Remove todos os itens do carrinho de compras do cliente autenticado.
                    
                    **Importante:**
                    - O carrinho não é deletado, apenas os itens são removidos
                    - O carrinho permanece vazio após esta operação
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem limpar seus carrinhos
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Carrinho limpo com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> clearMyShopping() {
        shoppingService.clearShopping();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar todos os carrinhos",
            description = """
                    Retorna uma lista de todos os carrinhos de compras do sistema.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de carrinhos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ShoppingResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<ShoppingResponseDTO>> getAllShoppings() {
        List<ShoppingResponseDTO> shoppings = shoppingService.getAllShoppings();
        return ResponseEntity.ok(shoppings);
    }
}

