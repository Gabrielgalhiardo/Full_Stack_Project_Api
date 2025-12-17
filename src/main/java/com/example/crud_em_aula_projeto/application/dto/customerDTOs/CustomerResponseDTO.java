package com.example.crud_em_aula_projeto.application.dto.customerDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Customer;

import java.util.UUID;

public record CustomerResponseDTO(
        UUID id,
        String name,
        String email,
        Boolean active
) {
    // Construtor que recebe Entity (Entity → DTO)
    public CustomerResponseDTO(Customer customer) {
        this(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getActive()
        );
    }
}
