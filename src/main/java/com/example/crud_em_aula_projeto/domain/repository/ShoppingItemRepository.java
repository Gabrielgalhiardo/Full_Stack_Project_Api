package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {
    List<ShoppingItem> findByShoppingId(UUID shoppingId);
    Optional<ShoppingItem> findByShoppingIdAndProductId(UUID shoppingId, UUID productId);
}

