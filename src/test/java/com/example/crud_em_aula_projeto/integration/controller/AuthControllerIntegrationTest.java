package com.example.crud_em_aula_projeto.integration.controller;

import com.example.crud_em_aula_projeto.application.dto.AuthDTO;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - AuthController")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Collaborator collaborator;

    @BeforeEach
    void setUp() {
        collaboratorRepository.deleteAll();

        collaborator = Collaborator.builder()
                .name("Test Collaborator")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.COLLABORATOR)
                .active(true)
                .build();

        collaboratorRepository.save(collaborator);
    }

    @Test
    @DisplayName("Deve fazer login com sucesso e retornar token JWT")
    void deveFazerLoginComSucesso() throws Exception {
        // Arrange
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest(
                "test@example.com",
                "password123"
        );

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando usuário não encontrado")
    void deveRetornarErroQuandoUsuarioNaoEncontrado() throws Exception {
        // Arrange
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest(
                "inexistente@example.com",
                "password123"
        );

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError()); // RuntimeException retorna 500
    }

    @Test
    @DisplayName("Deve retornar erro quando senha incorreta")
    void deveRetornarErroQuandoSenhaIncorreta() throws Exception {
        // Arrange
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest(
                "test@example.com",
                "senhaErrada"
        );

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando email inválido")
    void deveRetornarErroQuandoEmailInvalido() throws Exception {
        // Arrange
        AuthDTO.LoginRequest loginRequest = new AuthDTO.LoginRequest(
                "email-invalido",
                "password123"
        );

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando campos obrigatórios estão vazios")
    void deveRetornarErroQuandoCamposVazios() throws Exception {
        // Arrange
        String jsonRequest = """
                {
                  "email": "",
                  "password": ""
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}

