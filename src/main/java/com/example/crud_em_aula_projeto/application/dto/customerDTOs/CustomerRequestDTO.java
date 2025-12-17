package com.example.crud_em_aula_projeto.application.dto.customerDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;

public record CustomerRequestDTO(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password
) {
    // Método estático para converter DTO → Entity
    // Requer PasswordEncoder para codificar a senha
    public Customer toEntity(PasswordEncoder passwordEncoder) {
        return Customer.builder()
                .name(this.name())
                .email(this.email())
                .passwordHash(passwordEncoder.encode(this.password()))
                .role(Role.USER)
                .active(true)
                .build();
    }
}
