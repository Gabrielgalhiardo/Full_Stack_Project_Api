package com.example.crud_em_aula_projeto.infrastructure.config;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    // 1. Injetar o repositório correto
    private final CollaboratorRepository collaboratorRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${sistema.admin.email:admin@email.com}")
    private String adminEmail;

    @Value("${sistema.admin.senha:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // 2. Usar o collaboratorRepository para a busca
        collaboratorRepository.findByEmail(adminEmail).ifPresentOrElse(
                admin -> {
                    // A lógica para reativar, se necessário, pode ser mantida
                    if (!admin.getActive()) {
                        admin.setActive(true);
                        collaboratorRepository.save(admin);
                        System.out.println("✅ Usuário ADMIN reativado: " + adminEmail);
                    } else {
                        System.out.println("✅ Usuário ADMIN já existe e está ativo.");
                    }
                },
                () -> {
                    // 3. Criar uma instância de Collaborator
                    Collaborator admin = Collaborator.builder()
                            .name("Admin do Sistema") // 'nome' para 'name'
                            .email(adminEmail)
                            .passwordHash(passwordEncoder.encode(adminPassword)) // 'senhaHash' para 'passwordHash'
                            .role(Role.ADMIN)
                            .active(true) // Definir o estado inicial como ativo
                            .build();
                    collaboratorRepository.save(admin);
                    System.out.println("⚡ Usuário ADMIN padrão criado: " + adminEmail);
                }
        );
    }
}