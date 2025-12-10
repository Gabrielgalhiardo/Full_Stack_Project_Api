package com.example.crud_em_aula_projeto.application.dto.productDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;

import java.util.UUID;

public record MyProductDTO(
        UUID id,
        String title,
        String description,
        Double price,
        Integer quantity,
        ProductStatus productStatus,
        String imageUrl
) {
    public MyProductDTO(Product product) {
        this(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getProductStatus(),
                product.getImageUrl()
        );
    }
}
