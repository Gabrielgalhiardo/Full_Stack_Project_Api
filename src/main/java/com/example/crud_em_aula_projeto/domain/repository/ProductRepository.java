package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findAllByCollaboratorId(Long collaboratorId);

    List<Product> findAllByProductStatus(ProductStatus status);

    List<Product> findAllByProductCategoryAndProductStatus(ProductCategory category, ProductStatus status);

    List<Product> findAllByProductStatusIn(List<ProductStatus> statuses);

}































