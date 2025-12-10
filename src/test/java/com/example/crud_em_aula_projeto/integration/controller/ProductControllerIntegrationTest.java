package com.example.crud_em_aula_projeto.integration.controller;

import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductRequestDTO;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import com.example.crud_em_aula_projeto.domain.repository.UsuarioRepository;
import com.example.crud_em_aula_projeto.infrastructure.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - ProductController")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Collaborator collaborator;
    private Collaborator admin;
    private Customer customer;
    private String collaboratorToken;
    private String adminToken;
    private String customerToken;
    private Product product;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        collaboratorRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Criar colaborador
        collaborator = Collaborator.builder()
                .name("João Silva")
                .email("joao@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.COLLABORATOR)
                .active(true)
                .build();
        collaborator = collaboratorRepository.save(collaborator);
        collaboratorToken = jwtService.generateToken(collaborator.getEmail(), collaborator.getRole().name());

        // Criar admin (verificar se já existe)
        admin = collaboratorRepository.findByEmail("admin@test.com")
                .orElseGet(() -> {
                    Collaborator newAdmin = Collaborator.builder()
                            .name("Admin")
                            .email("admin@test.com")
                            .passwordHash(passwordEncoder.encode("admin123"))
                            .role(Role.ADMIN)
                            .active(true)
                            .build();
                    return collaboratorRepository.save(newAdmin);
                });
        adminToken = jwtService.generateToken(admin.getEmail(), admin.getRole().name());

        // Criar customer (USER) para testes de endpoints públicos
        Customer newCustomer = Customer.builder()
                .name("Cliente Teste")
                .email("cliente@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.USER)
                .active(true)
                .build();
        customer = (Customer) usuarioRepository.save(newCustomer);
        customerToken = jwtService.generateToken(customer.getEmail(), customer.getRole().name());

        // Criar produto
        product = Product.builder()
                .title("Smartphone")
                .description("Smartphone moderno")
                .price(2500.0)
                .quantity(10)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();
        product = productRepository.save(product);
    }

    @Test
    @DisplayName("Deve listar produtos públicos com autenticação")
    void deveListarProdutosPublicos() throws Exception {
        // Act & Assert - Endpoint requer autenticação (USER, COLLABORATOR ou ADMIN)
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").value("Smartphone"))
                .andExpect(jsonPath("$[0].price").value(2500.0));
    }

    @Test
    @DisplayName("Deve filtrar produtos por categoria")
    void deveFiltrarProdutosPorCategoria() throws Exception {
        // Arrange - Criar produto de outra categoria
        Product book = Product.builder()
                .title("Livro")
                .description("Livro interessante")
                .price(50.0)
                .quantity(5)
                .imageUrl("http://example.com/book.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.BOOKS)
                .collaborator(collaborator)
                .build();
        productRepository.save(book);

        // Act & Assert - Endpoint requer autenticação
        mockMvc.perform(get("/api/products/category/ELECTRONICS")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productCategory").value("ELECTRONICS"));
    }

    @Test
    @DisplayName("Deve criar produto com autenticação de colaborador")
    void deveCriarProdutoComAutenticacao() throws Exception {
        // Arrange
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Novo Produto",
                "Descrição do novo produto",
                1500.0,
                5,
                "http://example.com/new-product.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + collaboratorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Novo Produto"))
                .andExpect(jsonPath("$.price").value(1500.0));
    }

    @Test
    @DisplayName("Deve retornar 403 ao criar produto sem autenticação")
    void deveRetornar403AoCriarProdutoSemAutenticacao() throws Exception {
        // Arrange
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Novo Produto",
                "Descrição",
                1500.0,
                5,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve listar produtos do colaborador autenticado")
    void deveListarProdutosDoColaborador() throws Exception {
        // Arrange - Criar mais produtos do colaborador
        Product product2 = Product.builder()
                .title("Notebook")
                .description("Notebook gamer")
                .price(5000.0)
                .quantity(3)
                .imageUrl("http://example.com/notebook.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();
        productRepository.save(product2);

        // Act & Assert
        mockMvc.perform(get("/api/products/my-products")
                        .header("Authorization", "Bearer " + collaboratorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Deve atualizar produto do colaborador")
    void deveAtualizarProduto() throws Exception {
        // Arrange
        ProductRequestDTO updateDTO = new ProductRequestDTO(
                "Smartphone Atualizado",
                "Nova descrição",
                3000.0,
                15,
                "http://example.com/updated.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        // Act & Assert
        mockMvc.perform(put("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + collaboratorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Smartphone Atualizado"))
                .andExpect(jsonPath("$.price").value(3000.0));
    }

    @Test
    @DisplayName("Deve retornar 403 ao atualizar produto de outro colaborador")
    void deveRetornar403AoAtualizarProdutoDeOutroColaborador() throws Exception {
        // Arrange
        Collaborator outroColaborador = Collaborator.builder()
                .name("Outro Colaborador")
                .email("outro@test.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .role(Role.COLLABORATOR)
                .active(true)
                .build();
        outroColaborador = collaboratorRepository.save(outroColaborador);
        String outroToken = jwtService.generateToken(outroColaborador.getEmail(), outroColaborador.getRole().name());

        ProductRequestDTO updateDTO = new ProductRequestDTO(
                "Tentativa de Atualização",
                "Descrição",
                1000.0,
                5,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        // Act & Assert
        mockMvc.perform(put("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + outroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve deletar produto (soft delete)")
    void deveDeletarProduto() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/" + product.getId())
                        .header("Authorization", "Bearer " + collaboratorToken))
                .andExpect(status().isNoContent());

        // Verificar que o produto foi marcado como DISCONTINUED
        Product deletedProduct = productRepository.findById(product.getId()).orElseThrow();
        assert deletedProduct.getProductStatus() == ProductStatus.DISCONTINUED;
    }

    @Test
    @DisplayName("Deve listar produtos inativos (apenas admin)")
    void deveListarProdutosInativos() throws Exception {
        // Arrange - Criar produto inativo
        Product inactiveProduct = Product.builder()
                .title("Produto Inativo")
                .description("Descrição")
                .price(1000.0)
                .quantity(0)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.DISCONTINUED)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();
        productRepository.save(inactiveProduct);

        // Act & Assert
        mockMvc.perform(get("/api/products/inactive")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].productStatus").value("DISCONTINUED"));
    }

    @Test
    @DisplayName("Deve retornar 403 ao listar produtos inativos sem ser admin")
    void deveRetornar403AoListarProdutosInativosSemSerAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/inactive")
                        .header("Authorization", "Bearer " + collaboratorToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

