package com.example.crud_em_aula_projeto.application.dto.productDTOs;

import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductRequestDTO(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @Positive Double price,
        @NotNull @Positive Integer quantity,
        @NotBlank String imageUrl,
        @NotNull ProductStatus productStatus,
        @NotNull ProductCategory productCategory
) {
}
