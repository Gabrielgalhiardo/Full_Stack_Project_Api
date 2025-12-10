package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.collaboratorDTOs.CollaboratorResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - CollaboratorService")
class CollaboratorServiceTest {

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CollaboratorService collaboratorService;

    private Collaborator collaborator;
    private UUID collaboratorId;
    private CollaboratorRequestDTO requestDTO;

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

        requestDTO = new CollaboratorRequestDTO(
                "João Silva",
                "joao@test.com",
                "senha123"
        );
    }

    @Test
    @DisplayName("Deve criar colaborador com sucesso")
    void deveCriarColaboradorComSucesso() {
        // Arrange
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(collaboratorRepository.save(any(Collaborator.class))).thenAnswer(invocation -> {
            Collaborator saved = invocation.getArgument(0);
            saved.setId(collaboratorId);
            return saved;
        });

        // Act
        CollaboratorResponseDTO result = collaboratorService.create(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva", result.name());
        assertEquals("joao@test.com", result.email());
        assertTrue(result.active());
        
        ArgumentCaptor<Collaborator> captor = ArgumentCaptor.forClass(Collaborator.class);
        verify(collaboratorRepository).save(captor.capture());
        Collaborator saved = captor.getValue();
        assertEquals("João Silva", saved.getName());
        assertEquals("joao@test.com", saved.getEmail());
        assertEquals(Role.COLLABORATOR, saved.getRole());
        assertTrue(saved.getActive());
        verify(passwordEncoder).encode("senha123");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar colaborador com email já existente")
    void deveLancarExcecaoAoCriarColaboradorComEmailExistente() {
        // Arrange
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                collaboratorService.create(requestDTO));

        assertEquals("Email already in use", exception.getMessage());
        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar colaborador por email com sucesso")
    void deveBuscarColaboradorPorEmail() {
        // Arrange
        when(collaboratorRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(collaborator));

        // Act
        CollaboratorResponseDTO result = collaboratorService.findByEmail("joao@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(collaboratorId, result.id());
        assertEquals("João Silva", result.name());
        assertEquals("joao@test.com", result.email());
        assertTrue(result.active());
        verify(collaboratorRepository).findByEmail("joao@test.com");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar colaborador inexistente por email")
    void deveLancarExcecaoAoBuscarColaboradorInexistentePorEmail() {
        // Arrange
        when(collaboratorRepository.findByEmail("inexistente@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> 
                collaboratorService.findByEmail("inexistente@test.com"));

        assertTrue(exception.getMessage().contains("Collaborator not found with email"));
        verify(collaboratorRepository).findByEmail("inexistente@test.com");
    }

    @Test
    @DisplayName("Deve listar todos os colaboradores ativos")
    void deveListarColaboradoresAtivos() {
        // Arrange
        Collaborator colaborador2 = Collaborator.builder()
                .id(UUID.randomUUID())
                .name("Maria Santos")
                .email("maria@test.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        List<Collaborator> colaboradores = List.of(collaborator, colaborador2);
        when(collaboratorRepository.findAllByActiveTrue()).thenReturn(colaboradores);

        // Act
        List<CollaboratorResponseDTO> result = collaboratorService.findAllActive();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("João Silva", result.get(0).name());
        assertEquals("Maria Santos", result.get(1).name());
        verify(collaboratorRepository).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Deve buscar colaborador por ID com sucesso")
    void deveBuscarColaboradorPorId() {
        // Arrange
        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));

        // Act
        CollaboratorResponseDTO result = collaboratorService.findById(collaboratorId);

        // Assert
        assertNotNull(result);
        assertEquals(collaboratorId, result.id());
        assertEquals("João Silva", result.name());
        assertEquals("joao@test.com", result.email());
        verify(collaboratorRepository).findById(collaboratorId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar colaborador inexistente por ID")
    void deveLancarExcecaoAoBuscarColaboradorInexistentePorId() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(collaboratorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> 
                collaboratorService.findById(idInexistente));

        assertTrue(exception.getMessage().contains("Collaborator not found with id"));
        verify(collaboratorRepository).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve atualizar colaborador com sucesso")
    void deveAtualizarColaborador() {
        // Arrange
        CollaboratorRequestDTO updateDTO = new CollaboratorRequestDTO(
                "João Silva Atualizado",
                "joao.novo@test.com",
                "novaSenha123"
        );

        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findByEmail("joao.novo@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$newEncodedPassword");
        when(collaboratorRepository.save(any(Collaborator.class))).thenReturn(collaborator);

        // Act
        CollaboratorResponseDTO result = collaboratorService.update(collaboratorId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", collaborator.getName());
        assertEquals("joao.novo@test.com", collaborator.getEmail());
        verify(collaboratorRepository).findById(collaboratorId);
        verify(collaboratorRepository).save(collaborator);
        verify(passwordEncoder).encode("novaSenha123");
    }

    @Test
    @DisplayName("Deve atualizar colaborador sem alterar senha quando senha vazia")
    void deveAtualizarColaboradorSemAlterarSenha() {
        // Arrange
        CollaboratorRequestDTO updateDTO = new CollaboratorRequestDTO(
                "João Silva Atualizado",
                "joao@test.com",
                ""
        );

        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.save(any(Collaborator.class))).thenReturn(collaborator);

        // Act
        CollaboratorResponseDTO result = collaboratorService.update(collaboratorId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("João Silva Atualizado", collaborator.getName());
        verify(collaboratorRepository).save(collaborator);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com email já em uso por outro colaborador")
    void deveLancarExcecaoAoAtualizarComEmailJaEmUso() {
        // Arrange
        Collaborator outroColaborador = Collaborator.builder()
                .id(UUID.randomUUID())
                .name("Outro Colaborador")
                .email("outro@test.com")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        CollaboratorRequestDTO updateDTO = new CollaboratorRequestDTO(
                "João Silva",
                "outro@test.com",
                "senha123"
        );

        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.findByEmail("outro@test.com")).thenReturn(Optional.of(outroColaborador));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                collaboratorService.update(collaboratorId, updateDTO));

        assertEquals("Email already in use", exception.getMessage());
        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar colaborador (soft delete)")
    void deveDesativarColaborador() {
        // Arrange
        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.save(any(Collaborator.class))).thenReturn(collaborator);

        // Act
        collaboratorService.deleteCollaborator(collaboratorId);

        // Assert
        assertFalse(collaborator.getActive());
        verify(collaboratorRepository).findById(collaboratorId);
        verify(collaboratorRepository).save(collaborator);
    }

    @Test
    @DisplayName("Deve reativar colaborador")
    void deveReativarColaborador() {
        // Arrange
        collaborator.setActive(false);
        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.of(collaborator));
        when(collaboratorRepository.save(any(Collaborator.class))).thenReturn(collaborator);

        // Act
        collaboratorService.activate(collaboratorId);

        // Assert
        assertTrue(collaborator.getActive());
        verify(collaboratorRepository).findById(collaboratorId);
        verify(collaboratorRepository).save(collaborator);
    }

    @Test
    @DisplayName("Deve lançar exceção ao desativar colaborador inexistente")
    void deveLancarExcecaoAoDesativarColaboradorInexistente() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(collaboratorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                collaboratorService.deleteCollaborator(idInexistente));
        verify(collaboratorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao reativar colaborador inexistente")
    void deveLancarExcecaoAoReativarColaboradorInexistente() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(collaboratorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                collaboratorService.activate(idInexistente));
        verify(collaboratorRepository, never()).save(any());
    }
}

