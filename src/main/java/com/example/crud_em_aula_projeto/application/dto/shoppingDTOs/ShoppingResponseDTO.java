package com.example.crud_em_aula_projeto.application.dto.shoppingDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Shopping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ShoppingResponseDTO(
        UUID id,
        UUID customerId,
        List<ShoppingItemResponseDTO> items,
        Double totalAmount
) {
    public ShoppingResponseDTO(Shopping shopping) {
        this(
                shopping.getId(),
                shopping.getCustomer() != null ? shopping.getCustomer().getId() : null,
                shopping.getItems() != null 
                        ? shopping.getItems().stream()
                                .map(ShoppingItemResponseDTO::new)
                                .collect(Collectors.toList())
                        : List.of(),
                shopping.getTotalAmount()
        );
    }
}

