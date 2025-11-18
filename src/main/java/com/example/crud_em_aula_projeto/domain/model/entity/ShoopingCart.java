package com.example.crud_em_aula_projeto.domain.model.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "shopping_carts")
public class ShoopingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private List<Product> listOfProducts;
}
