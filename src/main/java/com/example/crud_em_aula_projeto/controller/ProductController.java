package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.productDTOs.MyProductDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductPublicDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductRequestDTO;
import com.example.crud_em_aula_projeto.application.service.ProductService;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos.")
public class ProductController {

    private final ProductService productService;


    @GetMapping
    @Operation(
            summary = "Listar produtos para a vitrine",
            description = """
                    Retorna uma lista de todos os produtos com status 'AVAILABLE' (disponíveis para venda).
                    
                    Este endpoint é público e pode ser acessado por usuários autenticados (USER, COLLABORATOR, ADMIN).
                    Retorna apenas informações públicas dos produtos, sem dados sensíveis.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de produtos disponíveis retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = ProductPublicDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                                        "title": "Smartphone Moderno",
                                                        "description": "Último lançamento com 256GB",
                                                        "price": 4500.00,
                                                        "imageUrl": "http://example.com/phone.png",
                                                        "productCategory": "ELECTRONICS",
                                                        "collaboratorId": "660e8400-e29b-41d4-a716-446655440001",
                                                        "collaboratorName": "João Silva"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
            }
    )
    public ResponseEntity<List<ProductPublicDTO>> getAllPublicProducts() {
        List<ProductPublicDTO> products = productService.findAllPublicProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Filtrar produtos da vitrine por categoria",
            description = """
                    Retorna uma lista de produtos públicos filtrados por uma categoria específica.
                    
                    As categorias disponíveis são:
                    - ELECTRONICS (Eletrônicos)
                    - BOOKS (Livros)
                    - CLOTHING (Roupas)
                    - FOOD (Alimentos)
                    - SPORTS (Esportes)
                    - HOME (Casa e Decoração)
                    - TOYS (Brinquedos)
                    - OTHER (Outros)
                    
                    O parâmetro é case-insensitive (aceita maiúsculas ou minúsculas).
                    """,
            parameters = {
                    @Parameter(
                            name = "category",
                            description = "Categoria do produto (case-insensitive). Valores válidos: ELECTRONICS, BOOKS, CLOTHING, FOOD, SPORTS, HOME, TOYS, OTHER",
                            required = true,
                            example = "ELECTRONICS",
                            schema = @Schema(type = "string", allowableValues = {
                                    "ELECTRONICS", "BOOKS", "CLOTHING", "FOOD", "SPORTS", "HOME", "TOYS", "OTHER"
                            })
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de produtos da categoria retornada com sucesso.",
                            content = @Content(schema = @Schema(implementation = ProductPublicDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Categoria inválida fornecida. Use uma das categorias válidas.",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
            }
    )
    public ResponseEntity<List<ProductPublicDTO>> getPublicProductsByCategory(@PathVariable String category) {
        try {
            ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
            List<ProductPublicDTO> products = productService.findAllPublicProductsByCategory(productCategory);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --- ENDPOINTS DE ADMIN ---

    @GetMapping("/inactive")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[ADMIN] Listar produtos inativos",
            description = """
                    Retorna todos os produtos que não estão disponíveis para venda.
                    
                    **Inclui produtos com status:**
                    - UNAVAILABLE (Indisponível)
                    - OUT_OF_STOCK (Fora de estoque)
                    - DISCONTINUED (Descontinuado)
                    - DELETED (Deletado logicamente)
                    
                    **Acesso restrito:**
                    - Apenas usuários com role ADMIN podem acessar este endpoint
                    - Requer autenticação JWT
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de produtos inativos retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = MyProductDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                                        "title": "Produto Descontinuado",
                                                        "description": "Este produto não está mais disponível para venda.",
                                                        "price": 1000.00,
                                                        "quantity": 0,
                                                        "productStatus": "DISCONTINUED",
                                                        "imageUrl": "http://example.com/discontinued.png"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores podem acessar este endpoint.", content = @Content)
            }
    )
    public ResponseEntity<List<MyProductDTO>> getInactiveProducts() {
        List<MyProductDTO> inactiveProducts = productService.findAllInactiveProducts();
        return ResponseEntity.ok(inactiveProducts);
    }

    // --- ENDPOINTS DE COLABORADOR ---

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[COLABORADOR] Cadastrar um novo produto",
            description = "Cria um novo produto. O produto será associado ao colaborador autenticado.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do produto para cadastro.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Smartphone Moderno",
                                      "description": "Último lançamento com 256GB de armazenamento.",
                                      "price": 4500.00,
                                      "quantity": 50,
                                      "imageUrl": "http://example.com/phone.png",
                                      "productStatus": "AVAILABLE",
                                      "productCategory": "ELECTRONICS"
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Produto cadastrado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = MyProductDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "550e8400-e29b-41d4-a716-446655440000",
                                                      "title": "Smartphone Moderno",
                                                      "description": "Último lançamento com 256GB de armazenamento.",
                                                      "price": 4500.00,
                                                      "quantity": 50,
                                                      "productStatus": "AVAILABLE",
                                                      "imageUrl": "http://example.com/phone.png"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Erro de validação ou violação de regra de negócio.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content)
            }
    )
    public ResponseEntity<MyProductDTO> createProduct(@RequestBody @Valid ProductRequestDTO requestDTO) {
        MyProductDTO createdProduct = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[COLABORADOR] Atualizar um de seus produtos",
            description = """
                    Atualiza os dados de um produto existente.
                    
                    **Requisitos:**
                    - O usuário deve ser o dono do produto (colaborador que criou o produto)
                    - Requer autenticação JWT
                    - Apenas colaboradores e administradores podem atualizar produtos
                    
                    Todos os campos são opcionais, mas os fornecidos serão validados.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do produto a ser atualizado",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do produto",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Smartphone Moderno - Atualizado",
                                      "description": "Versão atualizada com 512GB de armazenamento.",
                                      "price": 5000.00,
                                      "quantity": 30,
                                      "imageUrl": "http://example.com/phone-v2.png",
                                      "productStatus": "AVAILABLE",
                                      "productCategory": "ELECTRONICS"
                                    }
                                    """)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Produto atualizado com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = MyProductDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "id": "550e8400-e29b-41d4-a716-446655440000",
                                                      "title": "Smartphone Moderno - Atualizado",
                                                      "description": "Versão atualizada com 512GB de armazenamento.",
                                                      "price": 5000.00,
                                                      "quantity": 30,
                                                      "productStatus": "AVAILABLE",
                                                      "imageUrl": "http://example.com/phone-v2.png"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de regra de negócio.", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado (não é o dono do produto ou não tem permissão).", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<MyProductDTO> updateMyProduct(@PathVariable UUID id, @RequestBody @Valid ProductRequestDTO requestDTO) {
        MyProductDTO updatedProduct = productService.updateMyProduct(id, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/my-products")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[COLABORADOR] Listar meus produtos",
            description = """
                    Retorna a lista completa de produtos cadastrados pelo colaborador autenticado.
                    
                    **Inclui:**
                    - Produtos ativos (AVAILABLE)
                    - Produtos inativos (UNAVAILABLE, OUT_OF_STOCK, etc.)
                    - Informações detalhadas incluindo quantidade e status
                    
                    Requer autenticação JWT e permissão de COLLABORATOR ou ADMIN.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de produtos do colaborador retornada com sucesso.",
                            content = @Content(
                                    schema = @Schema(implementation = MyProductDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    [
                                                      {
                                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                                        "title": "Smartphone Moderno",
                                                        "description": "Último lançamento com 256GB de armazenamento.",
                                                        "price": 4500.00,
                                                        "quantity": 50,
                                                        "productStatus": "AVAILABLE",
                                                        "imageUrl": "http://example.com/phone.png"
                                                      },
                                                      {
                                                        "id": "550e8400-e29b-41d4-a716-446655440001",
                                                        "title": "Notebook Gamer",
                                                        "description": "Alta performance para jogos e trabalho.",
                                                        "price": 7500.00,
                                                        "quantity": 15,
                                                        "productStatus": "AVAILABLE",
                                                        "imageUrl": "http://example.com/notebook.png"
                                                      }
                                                    ]
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas colaboradores e administradores podem acessar.", content = @Content)
            }
    )
    public ResponseEntity<List<MyProductDTO>> getMyProducts() {
        List<MyProductDTO> myProducts = productService.findProductsByAuthenticatedCollaborator();
        return ResponseEntity.ok(myProducts);
    }



    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[COLABORADOR] Deletar um produto",
            description = """
                    Realiza a exclusão lógica (soft delete) de um produto.
                    
                    **Importante:**
                    - O produto não é removido fisicamente do banco de dados
                    - O status do produto é alterado para indicar que foi deletado
                    - O usuário deve ser o dono do produto
                    - Requer autenticação JWT
                    - Apenas colaboradores e administradores podem deletar produtos
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "UUID do produto a ser deletado",
                            required = true,
                            example = "550e8400-e29b-41d4-a716-446655440000"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso (sem conteúdo no corpo da resposta)."),
                    @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Acesso negado (não é o dono do produto ou não tem permissão).", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}