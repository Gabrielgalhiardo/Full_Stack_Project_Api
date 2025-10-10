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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos.")
public class ProductController {

    private final ProductService productService;

    // --- ENDPOINTS PÚBLICOS ---

    @GetMapping
    @Operation(
            summary = "Listar produtos para a vitrine",
            description = "Retorna uma lista de todos os produtos com status 'AVAILABLE'.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida.")
            }
    )
    public ResponseEntity<List<ProductPublicDTO>> getAllPublicProducts() {
        List<ProductPublicDTO> products = productService.findAllPublicProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Filtrar produtos da vitrine por categoria",
            description = "Retorna uma lista de produtos públicos filtrados por uma categoria específica.",
            parameters = {
                    @Parameter(name = "category", description = "Categoria do produto. Ex: ELECTRONICS, BOOKS", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida."),
                    @ApiResponse(responseCode = "400", description = "Categoria inválida fornecida.", content = @Content)
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
            description = "Retorna todos os produtos que não estão disponíveis para venda. Requer permissão de ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.")
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
                    @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Erro de validação ou violação de regra de negócio."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.")
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
            description = "Atualiza os dados de um produto. O usuário deve ser o dono do produto.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado (não é o dono)."),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado.")
            }
    )
    public ResponseEntity<MyProductDTO> updateMyProduct(@PathVariable Long id, @RequestBody @Valid ProductRequestDTO requestDTO) {
        MyProductDTO updatedProduct = productService.updateMyProduct(id, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/my-products")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "[COLABORADOR] Listar meus produtos",
            description = "Retorna a lista de produtos cadastrados pelo colaborador autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado.")
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
            description = "Realiza a exclusão lógica (soft delete) de um produto. O usuário deve ser o dono do produto.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Produto deletado com sucesso."),
                    @ApiResponse(responseCode = "403", description = "Acesso negado (não é o dono)."),
                    @ApiResponse(responseCode = "404", description = "Produto não encontrado.")
            }
    )
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}