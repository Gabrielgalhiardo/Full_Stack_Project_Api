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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity // Habilita anotações como @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. ROTAS PÚBLICAS (AQUI ESTÁ A CORREÇÃO MAIS IMPORTANTE)
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui.html",      // A página HTML principal do Swagger
                                "/swagger-ui/**",        // Os recursos (CSS, JS, etc.) do Swagger
                                "/v3/api-docs/**"       // A especificação da API em JSON que o Swagger lê
                        ).permitAll()
                        // Cadastro de cliente público
                        .requestMatchers(HttpMethod.POST, "/api/customers").permitAll()
                        // Rotas públicas de produtos (GET)
                        .requestMatchers(HttpMethod.GET, "/api/products").hasAnyRole("USER", "COLLABORATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/products/category/**").hasAnyRole("USER", "COLLABORATOR", "ADMIN")

                        // 2. ROTAS DE CLIENTE (USER)
                        .requestMatchers("/api/cart/**").hasAnyRole("USER", "COLLABORATOR", "ADMIN")
                        // Rotas de carrinho de compras (USER)
                        .requestMatchers("/api/shopping-items/**").hasAnyRole("USER", "COLLABORATOR", "ADMIN")
                        // Rotas de shopping - ordem importa: específicas primeiro
                        .requestMatchers(HttpMethod.GET, "/api/shopping").hasRole("ADMIN") // Listar todos (apenas ADMIN)
                        .requestMatchers("/api/shopping/**").hasAnyRole("USER", "COLLABORATOR", "ADMIN") // Outras rotas de shopping
                        // Rotas de pedidos - ordem importa: específicas primeiro
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN") // Listar todos (apenas ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**/status").hasRole("ADMIN") // Atualizar status (apenas ADMIN)
                        .requestMatchers("/api/orders/my-sales").hasAnyRole("COLLABORATOR", "ADMIN") // Ver minhas vendas (COLLABORATOR)
                        .requestMatchers("/api/orders/sales/**").hasAnyRole("COLLABORATOR", "ADMIN") // Ver venda específica (COLLABORATOR)
                        .requestMatchers("/api/orders/**").hasAnyRole("USER", "COLLABORATOR", "ADMIN") // Outras rotas de pedidos

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3001"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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