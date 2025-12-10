package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.dto.AuthDTO;
import com.example.crud_em_aula_projeto.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Operation(
            summary = "Realizar Login",
            description = """
                    Autentica um usuário com e-mail e senha e retorna um token JWT.
                    
                    O token retornado deve ser usado em requisições subsequentes no header:
                    `Authorization: Bearer {token}`
                    
                    O token expira após 1 hora (3600 segundos).
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais de login",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthDTO.LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login de Cliente",
                                    value = """
                                            {
                                              "email": "cliente@exemplo.com",
                                              "password": "senha123"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login bem-sucedido, token JWT retornado.",
                            content = @Content(
                                    schema = @Schema(implementation = AuthDTO.TokenResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Credenciais inválidas, usuário não encontrado ou dados de entrada inválidos.",
                            content = @Content(
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "message": "Credenciais inválidas"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Não autorizado - credenciais incorretas.",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<AuthDTO.TokenResponse> login(@RequestBody @Valid AuthDTO.LoginRequest req) {
        String token = auth.login(req);
        return ResponseEntity.ok(new AuthDTO.TokenResponse(token));
    }
}