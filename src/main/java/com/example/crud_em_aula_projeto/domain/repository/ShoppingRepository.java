package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Shopping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingRepository extends JpaRepository<Shopping, UUID> {
    Optional<Shopping> findByCustomerId(UUID customerId);
}

