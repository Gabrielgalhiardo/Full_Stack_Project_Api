package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.orderDTOs.OrderResponseDTO;
import com.example.crud_em_aula_projeto.application.dto.orderDTOs.SalesResponseDTO;
import com.example.crud_em_aula_projeto.application.service.OrderService;
import com.example.crud_em_aula_projeto.domain.model.enuns.OrderStatus;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos (User)", description = "Endpoints para o gerenciamento de pedidos e compras.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(
            summary = "[USER] Finalizar compra do carrinho",
            description = """
                    Cria um novo pedido a partir dos itens do carrinho de compras.
                    
                    **Processo:**
                    1. Valida se o carrinho não está vazio
                    2. Valida estoque de todos os produtos
                    3. Cria o pedido com status PENDING
                    4. Atualiza o estoque dos produtos
                    5. Limpa o carrinho após criar o pedido
                    
                    **Validações:**
                    - Carrinho não pode estar vazio
                    - Todos os produtos devem estar disponíveis
                    - Estoque deve ser suficiente para todos os itens
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem finalizar compras
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Pedido criado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "990e8400-e29b-41d4-a716-446655440001",
                                                      "customerId": "550e8400-e29b-41d4-a716-446655440000",
                                                      "customerName": "Maria Silva",
                                                      "items": [
                                                        {
                                                          "id": "aa0e8400-e29b-41d4-a716-446655440002",
                                                          "productId": "880e8400-e29b-41d4-a716-446655440003",
                                                          "productTitle": "Produto Exemplo",
                                                          "productImageUrl": "http://example.com/image.jpg",
                                                          "unitPrice": 99.99,
                                                          "quantity": 2,
                                                          "subTotal": 199.98
                                                        }
                                                      ],
                                                      "status": "PENDING",
                                                      "totalAmount": 199.98,
                                                      "createdAt": "2024-01-15T10:30:00",
                                                      "updatedAt": "2024-01-15T10:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Carrinho vazio, estoque insuficiente ou produto indisponível.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Carrinho de compras não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<OrderResponseDTO> checkout() {
        OrderResponseDTO order = orderService.createOrderFromShopping();
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/my-orders")
    @Operation(
            summary = "[USER] Ver todas as minhas compras",
            description = """
                    Retorna uma lista de todos os pedidos do cliente autenticado.
                    
                    **Ordenação:**
                    - Pedidos mais recentes aparecem primeiro
                    - Ordenado por data de criação (descendente)
                    
                    **Informações retornadas:**
                    - ID do pedido
                    - Itens do pedido com detalhes
                    - Status do pedido
                    - Valor total
                    - Datas de criação e atualização
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Retorna apenas os pedidos do próprio cliente
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de pedidos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
            }
    )
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        List<OrderResponseDTO> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "[USER] Obter pedido por ID",
            description = """
                    Retorna um pedido específico pelo seu UUID.
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem acessar
                    - Apenas o dono do pedido pode acessá-lo
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do pedido",
                            required = true,
                            example = "990e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Pedido encontrado.",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para acessar este pedido.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable UUID id) {
        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "[USER] Cancelar pedido",
            description = """
                    Cancela um pedido do cliente autenticado.
                    
                    **Processo:**
                    1. Valida se o pedido pode ser cancelado
                    2. Restaura o estoque dos produtos
                    3. Altera o status para CANCELLED
                    
                    **Regras:**
                    - Apenas pedidos com status PENDING ou CONFIRMED podem ser cancelados
                    - Pedidos DELIVERED não podem ser cancelados
                    - O estoque dos produtos é restaurado automaticamente
                    
                    **Acesso restrito:**
                    - Apenas usuários com role USER podem cancelar pedidos
                    - Apenas o dono do pedido pode cancelá-lo
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do pedido a ser cancelado",
                            required = true,
                            example = "990e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Pedido não pode ser cancelado (já entregue ou já cancelado).", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado ou sem permissão para cancelar este pedido.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Atualizar status do pedido",
            description = """
                    Atualiza o status de um pedido (apenas para administradores).
                    
                    **Status possíveis:**
                    - PENDING: Pendente
                    - CONFIRMED: Confirmado
                    - SHIPPED: Enviado
                    - DELIVERED: Entregue
                    - CANCELLED: Cancelado
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem atualizar status
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do pedido",
                            required = true,
                            example = "990e8400-e29b-41d4-a716-446655440001"
                    ),
                    @Parameter(
                            name = "status",
                            description = "Novo status do pedido",
                            required = true,
                            example = "CONFIRMED"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Status do pedido atualizado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Status inválido ou não permitido.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem atualizar status.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        OrderResponseDTO order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Listar todos os pedidos",
            description = """
                    Retorna uma lista de todos os pedidos do sistema.
                    
                    **Ordenação:**
                    - Pedidos mais recentes aparecem primeiro
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de pedidos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = OrderResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // --- ENDPOINTS PARA COLABORADOR (VENDAS) ---

    @GetMapping("/my-sales")
    @PreAuthorize("hasAnyRole('COLLABORATOR', 'ADMIN')")
    @Operation(
            summary = "[COLLABORATOR] Ver minhas vendas",
            description = """
                    Retorna uma lista de todas as vendas do colaborador autenticado.
                    
                    **Informações retornadas:**
                    - Apenas pedidos que contêm produtos do colaborador
                    - Apenas os itens que pertencem ao colaborador são exibidos
                    - Total do colaborador (apenas seus itens) e total do pedido completo
                    - Ordenado por data de criação (mais recentes primeiro)
                    
                    **Diferencial:**
                    - Mostra apenas os itens que são seus em cada pedido
                    - Calcula o total apenas dos seus produtos vendidos
                    - Permite acompanhar suas vendas mesmo em pedidos com múltiplos vendedores
                    
                    **Acesso restrito:**
                    - Apenas usuários com role COLLABORATOR ou ADMIN podem acessar
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de vendas retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = SalesResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "orderId": "990e8400-e29b-41d4-a716-446655440001",
                                                        "customerId": "550e8400-e29b-41d4-a716-446655440000",
                                                        "customerName": "Maria Silva",
                                                        "myItems": [
                                                          {
                                                            "id": "aa0e8400-e29b-41d4-a716-446655440002",
                                                            "productId": "880e8400-e29b-41d4-a716-446655440003",
                                                            "productTitle": "Meu Produto",
                                                            "productImageUrl": "http://example.com/image.jpg",
                                                            "unitPrice": 99.99,
                                                            "quantity": 2,
                                                            "subTotal": 199.98
                                                          }
                                                        ],
                                                        "orderStatus": "CONFIRMED",
                                                        "myTotalAmount": 199.98,
                                                        "orderTotalAmount": 299.97,
                                                        "createdAt": "2024-01-15T10:30:00",
                                                        "updatedAt": "2024-01-15T10:30:00"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas colaboradores podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<SalesResponseDTO>> getMySales() {
        List<SalesResponseDTO> sales = orderService.getMySales();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/sales/{id}")
    @PreAuthorize("hasAnyRole('COLLABORATOR', 'ADMIN')")
    @Operation(
            summary = "[COLLABORATOR] Ver venda específica por ID",
            description = """
                    Retorna uma venda específica pelo ID do pedido.
                    
                    **Informações retornadas:**
                    - Apenas os itens que pertencem ao colaborador são exibidos
                    - Total do colaborador e total do pedido completo
                    
                    **Acesso restrito:**
                    - Apenas usuários com role COLLABORATOR ou ADMIN podem acessar
                    - Apenas vendas que contêm produtos do colaborador podem ser acessadas
                    - Requer autenticação JWT
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do pedido",
                            required = true,
                            example = "990e8400-e29b-41d4-a716-446655440001"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Venda encontrada.",
                            content = @Content(
                                    schema = @Schema(implementation = SalesResponseDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Este pedido não contém produtos seus.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<SalesResponseDTO> getSaleById(@PathVariable UUID id) {
        SalesResponseDTO sale = orderService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }
}

