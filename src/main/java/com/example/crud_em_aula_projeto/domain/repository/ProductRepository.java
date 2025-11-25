package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {


    List<Product> findAllByCollaboratorId(UUID collaboratorId);

    @Query("SELECT p FROM Product p JOIN FETCH p.collaborator WHERE p.productStatus = :status")
    List<Product> findAllByProductStatus(@Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p JOIN FETCH p.collaborator WHERE p.productCategory = :category AND p.productStatus = :status")
    List<Product> findAllByProductCategoryAndProductStatus(@Param("category") ProductCategory category, @Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p JOIN FETCH p.collaborator WHERE p.productStatus IN :statuses")
    List<Product> findAllByProductStatusIn(@Param("statuses") List<ProductStatus> statuses);

    @Query("SELECT p FROM Product p JOIN FETCH p.collaborator WHERE p.collaborator.id = :collaboratorId AND p.productStatus IN :statuses")
    List<Product> findAllByCollaboratorIdAndProductStatusIn(@Param("collaboratorId") UUID collaboratorId, @Param("statuses") List<ProductStatus> statuses);

}































