package com.example.crud_em_aula_projeto.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI SonhoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Gestão de Sonhos")
                        .description("Cadastro e gestão de Sonhos.")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Gabriel")
                                .email("Gabriel@gmail.com"))
                );
    }
}
