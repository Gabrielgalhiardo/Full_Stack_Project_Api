package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.productDTOs.MyProductDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductPublicDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductRequestDTO;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import com.example.crud_em_aula_projeto.domain.service.ProductDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private ProductDomainService productDomainService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductService productService;

    private Collaborator collaborator;
    private Product product;
    private UUID productId;
    private UUID collaboratorId;

    @BeforeEach
    void setUp() {
        collaboratorId = UUID.randomUUID();
        productId = UUID.randomUUID();

        collaborator = Collaborator.builder()
                .id(collaboratorId)
                .name("João Silva")
                .email("joao@test.com")
                .passwordHash("hashedPassword")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        product = Product.builder()
                .id(productId)
                .title("Smartphone")
                .description("Smartphone moderno")
                .price(2500.0)
                .quantity(10)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    private void setupSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("joao@test.com");
    }

    @Test
    @DisplayName("Deve listar todos os produtos públicos disponíveis")
    void deveListarTodosProdutosPublicos() {
        // Arrange
        List<Product> products = List.of(product);
        when(productRepository.findAllByProductStatus(ProductStatus.AVAILABLE)).thenReturn(products);

        // Act
        List<ProductPublicDTO> result = productService.findAllPublicProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product.getTitle(), result.get(0).title());
        verify(productRepository).findAllByProductStatus(ProductStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Deve criar um novo produto com sucesso")
    void deveCriarNovoProduto() {
        // Arrange
        setupSecurityContext();
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Novo Produto",
                "Descrição do produto",
                1500.0,
                5,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));
        doNothing().when(productDomainService).validateProductLimit(collaborator);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        MyProductDTO result = productService.createProduct(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(product.getId(), result.id());
        assertEquals(product.getTitle(), result.title());
        verify(productDomainService).validateProductLimit(collaborator);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto quando colaborador não encontrado")
    void deveLancarExcecaoQuandoColaboradorNaoEncontrado() {
        // Arrange
        setupSecurityContext();
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Novo Produto",
                "Descrição",
                1500.0,
                5,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar produtos do colaborador autenticado")
    void deveListarProdutosDoColaborador() {
        // Arrange
        setupSecurityContext();
        List<Product> products = List.of(product);
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));
        when(productRepository.findAllByCollaboratorIdAndProductStatusIn(
                collaboratorId,
                List.of(ProductStatus.AVAILABLE, ProductStatus.OUT_OF_STOCK)
        )).thenReturn(products);

        // Act
        List<MyProductDTO> result = productService.findProductsByAuthenticatedCollaborator();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product.getId(), result.get(0).id());
        verify(collaboratorRepository).findByEmail("joao@test.com");
    }

    @Test
    @DisplayName("Deve listar produtos públicos por categoria")
    void deveListarProdutosPorCategoria() {
        // Arrange
        List<Product> products = List.of(product);
        when(productRepository.findAllByProductCategoryAndProductStatus(
                ProductCategory.ELECTRONICS,
                ProductStatus.AVAILABLE
        )).thenReturn(products);

        // Act
        List<ProductPublicDTO> result = productService.findAllPublicProductsByCategory(ProductCategory.ELECTRONICS);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findAllByProductCategoryAndProductStatus(
                ProductCategory.ELECTRONICS,
                ProductStatus.AVAILABLE
        );
    }

    @Test
    @DisplayName("Deve listar produtos inativos")
    void deveListarProdutosInativos() {
        // Arrange
        Product inactiveProduct = Product.builder()
                .id(UUID.randomUUID())
                .title("Produto Inativo")
                .description("Descrição")
                .price(1000.0)
                .quantity(0)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.DISCONTINUED)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();

        List<Product> products = List.of(inactiveProduct);
        when(productRepository.findAllByProductStatusIn(List.of(ProductStatus.DISCONTINUED)))
                .thenReturn(products);

        // Act
        List<MyProductDTO> result = productService.findAllInactiveProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ProductStatus.DISCONTINUED, result.get(0).productStatus());
        verify(productRepository).findAllByProductStatusIn(List.of(ProductStatus.DISCONTINUED));
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProduto() {
        // Arrange
        setupSecurityContext();
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Produto Atualizado",
                "Nova descrição",
                3000.0,
                20,
                "http://example.com/new-image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        MyProductDTO result = productService.updateMyProduct(productId, requestDTO);

        // Assert
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        assertEquals("Produto Atualizado", product.getTitle());
        assertEquals("Nova descrição", product.getDescription());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto inexistente")
    void deveLancarExcecaoAoAtualizarProdutoInexistente() {
        // Arrange
        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Produto Atualizado",
                "Descrição",
                3000.0,
                20,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                productService.updateMyProduct(productId, requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar produto de outro colaborador")
    void deveLancarExcecaoAoAtualizarProdutoDeOutroColaborador() {
        // Arrange
        setupSecurityContext();
        Collaborator outroColaborador = Collaborator.builder()
                .id(UUID.randomUUID())
                .name("Outro Colaborador")
                .email("outro@test.com")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        Product produtoDeOutro = Product.builder()
                .id(productId)
                .title("Produto de Outro")
                .description("Descrição")
                .price(1000.0)
                .quantity(5)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(outroColaborador)
                .build();

        ProductRequestDTO requestDTO = new ProductRequestDTO(
                "Produto Atualizado",
                "Descrição",
                3000.0,
                20,
                "http://example.com/image.jpg",
                ProductStatus.AVAILABLE,
                ProductCategory.ELECTRONICS
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(produtoDeOutro));
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> 
                productService.updateMyProduct(productId, requestDTO));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar produto com sucesso (soft delete)")
    void deveDeletarProduto() {
        // Arrange
        setupSecurityContext();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.deleteProduct(productId);

        // Assert
        assertEquals(ProductStatus.DISCONTINUED, product.getProductStatus());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void deveLancarExcecaoAoDeletarProdutoInexistente() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                productService.deleteProduct(productId));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto de outro colaborador")
    void deveLancarExcecaoAoDeletarProdutoDeOutroColaborador() {
        // Arrange
        setupSecurityContext();
        Collaborator outroColaborador = Collaborator.builder()
                .id(UUID.randomUUID())
                .name("Outro Colaborador")
                .email("outro@test.com")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        Product produtoDeOutro = Product.builder()
                .id(productId)
                .title("Produto de Outro")
                .description("Descrição")
                .price(1000.0)
                .quantity(5)
                .imageUrl("http://example.com/image.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(outroColaborador)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(produtoDeOutro));
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> 
                productService.deleteProduct(productId));
        verify(productRepository, never()).save(any());
    }
}

