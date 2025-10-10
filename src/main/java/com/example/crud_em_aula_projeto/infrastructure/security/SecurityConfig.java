package com.example.crud_em_aula_projeto.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // Habilita anotações como @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. ROTAS PÚBLICAS (AQUI ESTÁ A CORREÇÃO MAIS IMPORTANTE)
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui.html",      // A página HTML principal do Swagger
                                "/swagger-ui/**",        // Os recursos (CSS, JS, etc.) do Swagger
                                "/v3/api-docs/**"        // A especificação da API em JSON que o Swagger lê
                        ).permitAll()

                        // 2. ROTAS DE CLIENTE (USER)
                        .requestMatchers("/api/cart/**").hasRole("USER")

                        // 3. ROTAS DE ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/products/inactive").hasRole("ADMIN")
                        .requestMatchers("/api/collaborators/**").hasRole("ADMIN")

                        // 4. ROTAS DE COLABORADOR (Admin também tem acesso)
                        .requestMatchers("/api/products/my-products").hasAnyRole("COLLABORATOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/products").hasAnyRole("COLLABORATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("COLLABORATOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("COLLABORATOR", "ADMIN")

                        // 5. QUALQUER OUTRA ROTA
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}