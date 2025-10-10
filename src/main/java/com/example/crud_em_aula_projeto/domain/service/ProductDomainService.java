package com.example.crud_em_aula_projeto.domain.service;

import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductDomainService {

    private final ProductRepository productRepository;

    public ProductDomainService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void validateProductLimit(Collaborator collaborator) {
        long count = productRepository.findAllByCollaboratorId(collaborator.getId())
                .stream()
                .filter(p -> p.getProductStatus() == ProductStatus.AVAILABLE)
                .count();

        if (count >= 10) {
            throw new BusinessRuleException("Collaborator has reached the limit of 10 active products.");
        }
    }

}