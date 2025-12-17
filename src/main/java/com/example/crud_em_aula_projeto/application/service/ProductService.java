package com.example.crud_em_aula_projeto.application.service;


import com.example.crud_em_aula_projeto.application.dto.productDTOs.MyProductDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductPublicDTO;
import com.example.crud_em_aula_projeto.application.dto.productDTOs.ProductRequestDTO;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import com.example.crud_em_aula_projeto.domain.service.ProductDomainService; // Importando o novo Domain Service
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final ProductDomainService productDomainService;

    public ProductService(ProductRepository productRepository, CollaboratorRepository collaboratorRepository, ProductDomainService productDomainService) {
        this.productRepository = productRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.productDomainService = productDomainService;
    }

    @Transactional(readOnly = true)
    public List<ProductPublicDTO> findAllPublicProducts() {
        return productRepository.findAllByProductStatus(ProductStatus.AVAILABLE)
                .stream()
                .map(ProductPublicDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public MyProductDTO createProduct(ProductRequestDTO requestDTO) {
        Collaborator collaborator = getAuthenticatedCollaborator();

        productDomainService.validateProductLimit(collaborator);

        Product newProduct = requestDTO.toEntity(collaborator);
        Product savedProduct = productRepository.save(newProduct);
        return new MyProductDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<MyProductDTO> findProductsByAuthenticatedCollaborator() {
        Collaborator collaborator = getAuthenticatedCollaborator();
        List<ProductStatus> activeStatuses = List.of(ProductStatus.AVAILABLE, ProductStatus.OUT_OF_STOCK);
        return productRepository.findAllByCollaboratorIdAndProductStatusIn(collaborator.getId(), activeStatuses)
                .stream()
                .map(MyProductDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductPublicDTO> findAllPublicProductsByCategory(ProductCategory category) {
        return productRepository.findAllByProductCategoryAndProductStatus(category, ProductStatus.AVAILABLE)
                .stream()
                .map(ProductPublicDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MyProductDTO> findAllInactiveProducts() {
        List<ProductStatus> inactiveStatuses = List.of(ProductStatus.DISCONTINUED);
        return productRepository.findAllByProductStatusIn(inactiveStatuses)
                .stream()
                .map(MyProductDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public MyProductDTO updateMyProduct(UUID productId, ProductRequestDTO requestDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        checkOwnership(existingProduct);

        existingProduct.setTitle(requestDTO.title());
        existingProduct.setDescription(requestDTO.description());
        existingProduct.setPrice(requestDTO.price());
        existingProduct.setQuantity(requestDTO.quantity());
        existingProduct.setImageUrl(requestDTO.imageUrl());
        existingProduct.setProductStatus(requestDTO.productStatus());
        existingProduct.setProductCategory(requestDTO.productCategory());

        Product updatedProduct = productRepository.save(existingProduct);
        return new MyProductDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        checkOwnership(product);

        product.setProductStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
    }

    private Collaborator getAuthenticatedCollaborator() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return collaboratorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with email: " + email));
    }

    private void checkOwnership(Product product) {
        Collaborator collaborator = getAuthenticatedCollaborator();
        if (!product.getCollaborator().getId().equals(collaborator.getId())) {
            throw new AccessDeniedException("User does not have permission to modify this product");
        }
    }
}