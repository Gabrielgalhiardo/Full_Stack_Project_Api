package com.example.crud_em_aula_projeto.application.dto.shoppingDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.ShoppingItem;

import java.util.UUID;

public record ShoppingItemResponseDTO(
        UUID id,
        UUID productId,
        String productTitle,
        Double productPrice,
        Integer quantity,
        Double subTotal
) {
    public ShoppingItemResponseDTO(ShoppingItem item) {
        this(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getTitle() : null,
                item.getProduct() != null ? item.getProduct().getPrice() : null,
                item.getQuantity(),
                item.getSubTotal()
        );
    }
}

