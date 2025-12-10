package com.example.crud_em_aula_projeto.integration.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - CollaboratorRepository")
class CollaboratorRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    private Collaborator collaborator1;
    private Collaborator collaborator2;
    private Collaborator collaborator3;

    @BeforeEach
    void setUp() {
        collaborator1 = Collaborator.builder()
                .name("João Silva")
                .email("joao@test.com")
                .passwordHash("$2a$10$hashedPassword1")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        collaborator2 = Collaborator.builder()
                .name("Maria Santos")
                .email("maria@test.com")
                .passwordHash("$2a$10$hashedPassword2")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        collaborator3 = Collaborator.builder()
                .name("Pedro Costa")
                .email("pedro@test.com")
                .passwordHash("$2a$10$hashedPassword3")
                .role(Role.COLLABORATOR)
                .active(false)
                .build();

        collaborator1 = entityManager.persistAndFlush(collaborator1);
        collaborator2 = entityManager.persistAndFlush(collaborator2);
        collaborator3 = entityManager.persistAndFlush(collaborator3);
    }

    @Test
    @DisplayName("Deve encontrar colaborador por email")
    void deveEncontrarColaboradorPorEmail() {
        // Act
        Optional<Collaborator> found = collaboratorRepository.findByEmail("joao@test.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("João Silva", found.get().getName());
        assertEquals("joao@test.com", found.get().getEmail());
        assertEquals(Role.COLLABORATOR, found.get().getRole());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando email não existe")
    void deveRetornarOptionalVazioQuandoEmailNaoExiste() {
        // Act
        Optional<Collaborator> found = collaboratorRepository.findByEmail("inexistente@test.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Deve encontrar apenas colaboradores ativos")
    void deveEncontrarApenasColaboradoresAtivos() {
        // Act
        List<Collaborator> ativos = collaboratorRepository.findAllByActiveTrue();

        // Assert
        assertNotNull(ativos);
        assertEquals(2, ativos.size());
        assertTrue(ativos.stream().allMatch(Collaborator::getActive));
        assertTrue(ativos.stream().noneMatch(c -> c.getEmail().equals("pedro@test.com")));
    }

    @Test
    @DisplayName("Deve salvar novo colaborador")
    void deveSalvarNovoColaborador() {
        // Arrange
        Collaborator novoColaborador = Collaborator.builder()
                .name("Ana Lima")
                .email("ana@test.com")
                .passwordHash("$2a$10$hashedPassword4")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        // Act
        Collaborator saved = collaboratorRepository.save(novoColaborador);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(saved.getId());
        Collaborator found = collaboratorRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Ana Lima", found.getName());
        assertEquals("ana@test.com", found.getEmail());
    }

    @Test
    @DisplayName("Deve atualizar colaborador existente")
    void deveAtualizarColaboradorExistente() {
        // Arrange
        collaborator1.setName("João Silva Atualizado");
        collaborator1.setEmail("joao.novo@test.com");

        // Act
        Collaborator updated = collaboratorRepository.save(collaborator1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Collaborator found = collaboratorRepository.findById(updated.getId()).orElseThrow();
        assertEquals("João Silva Atualizado", found.getName());
        assertEquals("joao.novo@test.com", found.getEmail());
    }

    @Test
    @DisplayName("Deve desativar colaborador (soft delete)")
    void deveDesativarColaborador() {
        // Arrange
        collaborator1.setActive(false);

        // Act
        Collaborator updated = collaboratorRepository.save(collaborator1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Collaborator found = collaboratorRepository.findById(updated.getId()).orElseThrow();
        assertFalse(found.getActive());
        
        // Verificar que não aparece na lista de ativos
        List<Collaborator> ativos = collaboratorRepository.findAllByActiveTrue();
        assertTrue(ativos.stream().noneMatch(c -> c.getId().equals(collaborator1.getId())));
    }

    @Test
    @DisplayName("Deve reativar colaborador")
    void deveReativarColaborador() {
        // Arrange
        collaborator3.setActive(true);

        // Act
        Collaborator updated = collaboratorRepository.save(collaborator3);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Collaborator found = collaboratorRepository.findById(updated.getId()).orElseThrow();
        assertTrue(found.getActive());
        
        // Verificar que aparece na lista de ativos
        List<Collaborator> ativos = collaboratorRepository.findAllByActiveTrue();
        assertTrue(ativos.stream().anyMatch(c -> c.getId().equals(collaborator3.getId())));
    }

    @Test
    @DisplayName("Deve deletar colaborador permanentemente")
    void deveDeletarColaboradorPermanentemente() {
        // Act
        collaboratorRepository.delete(collaborator1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertFalse(collaboratorRepository.findById(collaborator1.getId()).isPresent());
    }

    @Test
    @DisplayName("Deve encontrar colaborador por ID")
    void deveEncontrarColaboradorPorId() {
        // Act
        Optional<Collaborator> found = collaboratorRepository.findById(collaborator1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("João Silva", found.get().getName());
        assertEquals("joao@test.com", found.get().getEmail());
    }
}

