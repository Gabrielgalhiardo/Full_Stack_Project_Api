package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.AuthDTO;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.entity.Usuario;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.UsuarioRepository;
import com.example.crud_em_aula_projeto.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AuthService")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private AuthDTO.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = Customer.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.USER)
                .active(true)
                .build();

        loginRequest = new AuthDTO.LoginRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso e retornar token JWT")
    void deveFazerLoginComSucesso() {
        // Arrange
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", usuario.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(usuario.getEmail(), usuario.getRole().name())).thenReturn(expectedToken);

        // Act
        String token = authService.login(loginRequest);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(usuarioRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", usuario.getPasswordHash());
        verify(jwtService).generateToken(usuario.getEmail(), usuario.getRole().name());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                authService.login(loginRequest));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando senha incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        // Arrange
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", usuario.getPasswordHash())).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> 
                authService.login(loginRequest));

        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(usuarioRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", usuario.getPasswordHash());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve fazer login com diferentes roles")
    void deveFazerLoginComDiferentesRoles() {
        // Arrange
        Usuario admin = Customer.builder()
                .id(UUID.randomUUID())
                .name("Admin User")
                .email("admin@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.ADMIN)
                .active(true)
                .build();

        AuthDTO.LoginRequest adminLogin = new AuthDTO.LoginRequest("admin@example.com", "password123");
        String expectedToken = "admin-token";

        when(usuarioRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("password123", admin.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(admin.getEmail(), Role.ADMIN.name())).thenReturn(expectedToken);

        // Act
        String token = authService.login(adminLogin);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(jwtService).generateToken(admin.getEmail(), Role.ADMIN.name());
    }

    @Test
    @DisplayName("Deve fazer login com colaborador")
    void deveFazerLoginComColaborador() {
        // Arrange
        Usuario collaborator = Customer.builder()
                .id(UUID.randomUUID())
                .name("Collaborator User")
                .email("collaborator@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        AuthDTO.LoginRequest collaboratorLogin = new AuthDTO.LoginRequest("collaborator@example.com", "password123");
        String expectedToken = "collaborator-token";

        when(usuarioRepository.findByEmail("collaborator@example.com")).thenReturn(Optional.of(collaborator));
        when(passwordEncoder.matches("password123", collaborator.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(collaborator.getEmail(), Role.COLLABORATOR.name())).thenReturn(expectedToken);

        // Act
        String token = authService.login(collaboratorLogin);

        // Assert
        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(jwtService).generateToken(collaborator.getEmail(), Role.COLLABORATOR.name());
    }
}

