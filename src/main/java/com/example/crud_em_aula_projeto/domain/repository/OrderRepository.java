package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Order;
import com.example.crud_em_aula_projeto.domain.model.enuns.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerId(UUID customerId);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi JOIN oi.product p WHERE p.collaborator.id = :collaboratorId ORDER BY o.createdAt DESC")
    List<Order> findOrdersByCollaboratorId(UUID collaboratorId);
}

