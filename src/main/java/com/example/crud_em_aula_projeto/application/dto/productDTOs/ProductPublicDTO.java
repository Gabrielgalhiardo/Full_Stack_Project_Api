package com.example.crud_em_aula_projeto.application.dto.productDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;

public record ProductPublicDTO(
        Long id,
        String title,
        String description,
        Double price,
        String imageUrl,
        ProductCategory productCategory,
        Long collaboratorId,
        String collaboratorName
) {
    public ProductPublicDTO(Product product) {
        this(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getProductCategory(),
                product.getCollaborator().getId(),
                product.getCollaborator().getName()
        );
    }
}
