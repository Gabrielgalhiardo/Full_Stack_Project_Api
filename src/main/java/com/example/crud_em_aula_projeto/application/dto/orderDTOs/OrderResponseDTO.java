package com.example.crud_em_aula_projeto.application.dto.orderDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Order;
import com.example.crud_em_aula_projeto.domain.model.enuns.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record OrderResponseDTO(
        UUID id,
        UUID customerId,
        String customerName,
        List<OrderItemResponseDTO> items,
        OrderStatus status,
        Double totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public OrderResponseDTO(Order order) {
        this(
                order.getId(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                order.getItems() != null 
                        ? order.getItems().stream()
                                .map(OrderItemResponseDTO::new)
                                .collect(Collectors.toList())
                        : List.of(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

