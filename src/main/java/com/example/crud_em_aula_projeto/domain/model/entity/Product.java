package com.example.crud_em_aula_projeto.domain.model.entity;

import com.example.crud_em_aula_projeto.domain.model.enuns.ProductCategory;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String title;

    @Column(nullable = false)
    protected String description;

    @Column(nullable = false)
    protected Double price;

    @Column(nullable = false)
    protected Integer quantity;

    @Column(nullable = false)
    protected String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected ProductStatus productStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @ManyToOne
    @JoinColumn(name = "shopping_cart_id")
    private ShoopingCart shoppingCart;

}
