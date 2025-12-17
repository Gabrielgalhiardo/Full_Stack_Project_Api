package com.example.crud_em_aula_projeto.application.dto.productDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
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
    // Método estático para converter DTO → Entity
    // Requer Collaborator para associar o produto
    public Product toEntity(Collaborator collaborator) {
        return Product.builder()
                .title(this.title())
                .description(this.description())
                .price(this.price())
                .quantity(this.quantity())
                .imageUrl(this.imageUrl())
                .productStatus(this.productStatus())
                .productCategory(this.productCategory())
                .collaborator(collaborator)
                .build();
    }
}
