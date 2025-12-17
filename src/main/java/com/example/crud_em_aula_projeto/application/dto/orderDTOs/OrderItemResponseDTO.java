package com.example.crud_em_aula_projeto.application.dto.orderDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.OrderItem;

import java.util.UUID;

public record OrderItemResponseDTO(
        UUID id,
        UUID productId,
        String productTitle,
        String productImageUrl,
        Double unitPrice,
        Integer quantity,
        Double subTotal
) {
    public OrderItemResponseDTO(OrderItem item) {
        this(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProduct() != null ? item.getProduct().getTitle() : null,
                item.getProduct() != null ? item.getProduct().getImageUrl() : null,
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubTotal()
        );
    }
}

