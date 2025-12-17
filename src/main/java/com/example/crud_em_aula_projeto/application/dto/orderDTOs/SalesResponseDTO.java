package com.example.crud_em_aula_projeto.application.dto.orderDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Order;
import com.example.crud_em_aula_projeto.domain.model.entity.OrderItem;
import com.example.crud_em_aula_projeto.domain.model.enuns.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record SalesResponseDTO(
        UUID orderId,
        UUID customerId,
        String customerName,
        List<OrderItemResponseDTO> myItems, // Apenas itens do colaborador
        OrderStatus orderStatus,
        Double myTotalAmount, // Total apenas dos itens do colaborador
        Double orderTotalAmount, // Total completo do pedido
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public SalesResponseDTO(Order order, UUID collaboratorId) {
        this(
                order.getId(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                // Filtra apenas os itens que pertencem ao colaborador
                order.getItems() != null 
                        ? order.getItems().stream()
                                .filter(item -> item.getProduct() != null 
                                        && item.getProduct().getCollaborator() != null
                                        && item.getProduct().getCollaborator().getId().equals(collaboratorId))
                                .map(OrderItemResponseDTO::new)
                                .collect(Collectors.toList())
                        : List.of(),
                order.getStatus(),
                // Calcula o total apenas dos itens do colaborador
                order.getItems() != null
                        ? order.getItems().stream()
                                .filter(item -> item.getProduct() != null 
                                        && item.getProduct().getCollaborator() != null
                                        && item.getProduct().getCollaborator().getId().equals(collaboratorId))
                                .mapToDouble(OrderItem::getSubTotal)
                                .sum()
                        : 0.0,
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

