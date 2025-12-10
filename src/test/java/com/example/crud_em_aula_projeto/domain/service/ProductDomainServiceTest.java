package com.example.crud_em_aula_projeto.domain.service;

import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ProductDomainService")
class ProductDomainServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductDomainService productDomainService;

    private Collaborator collaborator;
    private UUID collaboratorId;

    @BeforeEach
    void setUp() {
        collaboratorId = UUID.randomUUID();
        
        collaborator = Collaborator.builder()
                .id(collaboratorId)
                .name("João Silva")
                .email("joao@test.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Deve validar limite de produtos quando colaborador tem menos de 10 produtos ativos")
    void deveValidarLimiteQuandoColaboradorTemMenosDe10Produtos() {
        // Arrange
        List<Product> produtos = criarListaProdutos(5, ProductStatus.AVAILABLE);
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        assertDoesNotThrow(() -> productDomainService.validateProductLimit(collaborator));
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve validar limite quando colaborador tem exatamente 9 produtos ativos")
    void deveValidarLimiteQuandoColaboradorTem9Produtos() {
        // Arrange
        List<Product> produtos = criarListaProdutos(9, ProductStatus.AVAILABLE);
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        assertDoesNotThrow(() -> productDomainService.validateProductLimit(collaborator));
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando colaborador tem exatamente 10 produtos ativos")
    void deveLancarExcecaoQuandoColaboradorTem10ProdutosAtivos() {
        // Arrange
        List<Product> produtos = criarListaProdutos(10, ProductStatus.AVAILABLE);
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> 
                productDomainService.validateProductLimit(collaborator));

        assertEquals("Collaborator has reached the limit of 10 active products.", exception.getMessage());
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando colaborador tem mais de 10 produtos ativos")
    void deveLancarExcecaoQuandoColaboradorTemMaisDe10ProdutosAtivos() {
        // Arrange
        List<Product> produtos = criarListaProdutos(15, ProductStatus.AVAILABLE);
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> 
                productDomainService.validateProductLimit(collaborator));

        assertEquals("Collaborator has reached the limit of 10 active products.", exception.getMessage());
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve ignorar produtos inativos no cálculo do limite")
    void deveIgnorarProdutosInativosNoCalculoDoLimite() {
        // Arrange
        List<Product> produtos = new ArrayList<>();
        // 5 produtos ativos
        produtos.addAll(criarListaProdutos(5, ProductStatus.AVAILABLE));
        // 10 produtos inativos (não devem contar)
        produtos.addAll(criarListaProdutos(10, ProductStatus.DISCONTINUED));
        produtos.addAll(criarListaProdutos(5, ProductStatus.OUT_OF_STOCK));
        
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        assertDoesNotThrow(() -> productDomainService.validateProductLimit(collaborator));
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve contar apenas produtos com status AVAILABLE")
    void deveContarApenasProdutosComStatusAvailable() {
        // Arrange
        List<Product> produtos = new ArrayList<>();
        // 9 produtos AVAILABLE
        produtos.addAll(criarListaProdutos(9, ProductStatus.AVAILABLE));
        // 1 produto OUT_OF_STOCK (não deve contar)
        produtos.addAll(criarListaProdutos(1, ProductStatus.OUT_OF_STOCK));
        
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        assertDoesNotThrow(() -> productDomainService.validateProductLimit(collaborator));
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    @Test
    @DisplayName("Deve validar quando colaborador não tem produtos")
    void deveValidarQuandoColaboradorNaoTemProdutos() {
        // Arrange
        List<Product> produtos = new ArrayList<>();
        when(productRepository.findAllByCollaboratorId(collaboratorId)).thenReturn(produtos);

        // Act & Assert
        assertDoesNotThrow(() -> productDomainService.validateProductLimit(collaborator));
        verify(productRepository).findAllByCollaboratorId(collaboratorId);
    }

    private List<Product> criarListaProdutos(int quantidade, ProductStatus status) {
        List<Product> produtos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Product produto = Product.builder()
                    .id(UUID.randomUUID())
                    .title("Produto " + i)
                    .description("Descrição do produto " + i)
                    .price(100.0 + i)
                    .quantity(10)
                    .imageUrl("http://example.com/image" + i + ".jpg")
                    .productStatus(status)
                    .productCategory(ProductCategory.ELECTRONICS)
                    .collaborator(collaborator)
                    .build();
            produtos.add(produto);
        }
        return produtos;
    }
}

