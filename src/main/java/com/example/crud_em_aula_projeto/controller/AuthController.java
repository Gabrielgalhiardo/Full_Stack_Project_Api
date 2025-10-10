package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.AuthDTO;
import com.example.crud_em_aula_projeto.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Operações de autenticação e login.") // <-- Documentação
public class AuthController {

    private final AuthService auth;

    @PostMapping("/login")
    @Operation( // <-- Documentação
            summary = "Realizar Login",
            description = "Autentica um usuário com e-mail e senha e retorna um token JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login bem-sucedido, token retornado."),
                    @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou usuário não encontrado.", content = @Content)
            }
    )
    public ResponseEntity<AuthDTO.TokenResponse> login(@RequestBody @Valid AuthDTO.LoginRequest req) {
        String token = auth.login(req);
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }
}