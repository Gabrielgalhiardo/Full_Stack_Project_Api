package com.example.crud_em_aula_projeto.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Autenticação JWT. Use o endpoint /auth/login para obter um token. Inclua o token no header Authorization como: Bearer {token}"
)
public class SwaggerConfig {
    @Bean
    public OpenAPI productOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Gestão de Produtos e Colaboradores")
                        .description("""
                                API REST completa para gerenciamento de produtos, colaboradores e autenticação.
                                
                                ## Funcionalidades Principais
                                - **Autenticação**: Sistema de login com JWT
                                - **Produtos**: CRUD completo de produtos com categorias e status
                                - **Colaboradores**: Gerenciamento de contas de colaboradores (apenas Admin)
                                - **Carrinho**: Gerenciamento de carrinho de compras (em desenvolvimento)
                                
                                ## Permissões
                                - **Público**: Visualização de produtos disponíveis
                                - **USER**: Acesso ao carrinho de compras
                                - **COLLABORATOR**: Gerenciamento de seus próprios produtos
                                - **ADMIN**: Acesso total ao sistema
                                
                                ## Autenticação
                                Para usar endpoints protegidos, primeiro faça login em `/auth/login` e use o token JWT retornado no header `Authorization: Bearer {token}`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gabriel")
                                .email("gabriel@gmail.com")
                                .url("https://github.com/gabriel"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.exemplo.com")
                                .description("Servidor de Produção")
                ));
    }
}