package com.example.crud_em_aula_projeto.application.dto.shoppingDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ShoppingItemRequestDTO(
        @NotNull UUID productId,
        @NotNull @Positive Integer quantity
) {}

