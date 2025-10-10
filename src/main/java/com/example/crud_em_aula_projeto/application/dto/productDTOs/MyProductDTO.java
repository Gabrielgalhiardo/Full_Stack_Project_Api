package com.example.crud_em_aula_projeto.application.dto.productDTOs;

import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;

public record MyProductDTO(
        Long id,
        String title,
        Double price,
        Integer quantity,
        ProductStatus productStatus,
        String imageUrl
) {
    public MyProductDTO(Product product) {
        this(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getQuantity(),
                product.getProductStatus(),
                product.getImageUrl()
        );
    }
}
