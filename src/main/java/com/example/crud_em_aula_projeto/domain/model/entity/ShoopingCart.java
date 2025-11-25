package com.example.crud_em_aula_projeto.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shopping_carts")
public class ShoopingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> listOfProducts;
}
