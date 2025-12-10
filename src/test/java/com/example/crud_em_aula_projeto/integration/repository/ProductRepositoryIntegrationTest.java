package com.example.crud_em_aula_projeto.integration.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - ProductRepository")
class ProductRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Collaborator collaborator;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        collaborator = Collaborator.builder()
                .name("João Silva")
                .email("joao@test.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();
        collaborator = entityManager.persistAndFlush(collaborator);

        product1 = Product.builder()
                .title("Smartphone")
                .description("Smartphone moderno")
                .price(2500.0)
                .quantity(10)
                .imageUrl("http://example.com/phone.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();

        product2 = Product.builder()
                .title("Notebook")
                .description("Notebook gamer")
                .price(5000.0)
                .quantity(5)
                .imageUrl("http://example.com/notebook.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();

        product3 = Product.builder()
                .title("Livro")
                .description("Livro interessante")
                .price(50.0)
                .quantity(20)
                .imageUrl("http://example.com/book.jpg")
                .productStatus(ProductStatus.DISCONTINUED)
                .productCategory(ProductCategory.BOOKS)
                .collaborator(collaborator)
                .build();

        product1 = entityManager.persistAndFlush(product1);
        product2 = entityManager.persistAndFlush(product2);
        product3 = entityManager.persistAndFlush(product3);
    }

    @Test
    @DisplayName("Deve encontrar todos os produtos por status")
    void deveEncontrarProdutosPorStatus() {
        // Act
        List<Product> produtos = productRepository.findAllByProductStatus(ProductStatus.AVAILABLE);

        // Assert
        assertNotNull(produtos);
        assertEquals(2, produtos.size());
        assertTrue(produtos.stream().allMatch(p -> p.getProductStatus() == ProductStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Deve encontrar produtos por categoria e status")
    void deveEncontrarProdutosPorCategoriaEStatus() {
        // Act
        List<Product> produtos = productRepository.findAllByProductCategoryAndProductStatus(
                ProductCategory.ELECTRONICS,
                ProductStatus.AVAILABLE
        );

        // Assert
        assertNotNull(produtos);
        assertEquals(2, produtos.size());
        assertTrue(produtos.stream().allMatch(p -> 
                p.getProductCategory() == ProductCategory.ELECTRONICS &&
                p.getProductStatus() == ProductStatus.AVAILABLE));
    }

    @Test
    @DisplayName("Deve encontrar produtos por colaborador")
    void deveEncontrarProdutosPorColaborador() {
        // Act
        List<Product> produtos = productRepository.findAllByCollaboratorId(collaborator.getId());

        // Assert
        assertNotNull(produtos);
        assertEquals(3, produtos.size());
        assertTrue(produtos.stream().allMatch(p -> 
                p.getCollaborator().getId().equals(collaborator.getId())));
    }

    @Test
    @DisplayName("Deve encontrar produtos por colaborador e status")
    void deveEncontrarProdutosPorColaboradorEStatus() {
        // Act
        List<Product> produtos = productRepository.findAllByCollaboratorIdAndProductStatusIn(
                collaborator.getId(),
                List.of(ProductStatus.AVAILABLE, ProductStatus.OUT_OF_STOCK)
        );

        // Assert
        assertNotNull(produtos);
        assertEquals(2, produtos.size());
        assertTrue(produtos.stream().allMatch(p -> 
                p.getCollaborator().getId().equals(collaborator.getId()) &&
                (p.getProductStatus() == ProductStatus.AVAILABLE || 
                 p.getProductStatus() == ProductStatus.OUT_OF_STOCK)));
    }

    @Test
    @DisplayName("Deve encontrar produtos por múltiplos status")
    void deveEncontrarProdutosPorMultiplosStatus() {
        // Act
        List<Product> produtos = productRepository.findAllByProductStatusIn(
                List.of(ProductStatus.DISCONTINUED)
        );

        // Assert
        assertNotNull(produtos);
        assertEquals(1, produtos.size());
        assertEquals(ProductStatus.DISCONTINUED, produtos.get(0).getProductStatus());
    }

    @Test
    @DisplayName("Deve carregar colaborador junto com produto (JOIN FETCH)")
    void deveCarregarColaboradorComProduto() {
        // Act
        List<Product> produtos = productRepository.findAllByProductStatus(ProductStatus.AVAILABLE);

        // Assert
        assertNotNull(produtos);
        assertFalse(produtos.isEmpty());
        Product produto = produtos.get(0);
        assertNotNull(produto.getCollaborator());
        assertNotNull(produto.getCollaborator().getName());
        assertEquals("João Silva", produto.getCollaborator().getName());
    }

    @Test
    @DisplayName("Deve salvar novo produto")
    void deveSalvarNovoProduto() {
        // Arrange
        Product novoProduto = Product.builder()
                .title("Tablet")
                .description("Tablet moderno")
                .price(1500.0)
                .quantity(8)
                .imageUrl("http://example.com/tablet.jpg")
                .productStatus(ProductStatus.AVAILABLE)
                .productCategory(ProductCategory.ELECTRONICS)
                .collaborator(collaborator)
                .build();

        // Act
        Product saved = productRepository.save(novoProduto);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(saved.getId());
        Product found = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Tablet", found.getTitle());
        assertEquals(1500.0, found.getPrice());
    }

    @Test
    @DisplayName("Deve atualizar produto existente")
    void deveAtualizarProdutoExistente() {
        // Arrange
        product1.setTitle("Smartphone Atualizado");
        product1.setPrice(3000.0);

        // Act
        Product updated = productRepository.save(product1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Product found = productRepository.findById(updated.getId()).orElseThrow();
        assertEquals("Smartphone Atualizado", found.getTitle());
        assertEquals(3000.0, found.getPrice());
    }

    @Test
    @DisplayName("Deve deletar produto")
    void deveDeletarProduto() {
        // Act
        productRepository.delete(product1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertFalse(productRepository.findById(product1.getId()).isPresent());
    }
}

