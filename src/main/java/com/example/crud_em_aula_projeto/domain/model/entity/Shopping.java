package com.example.crud_em_aula_projeto.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shoppings")
public class Shopping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @OneToMany(mappedBy = "shopping", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ShoppingItem> items = new ArrayList<>();

    public Double getTotalAmount() {
        return items.stream()
                .mapToDouble(ShoppingItem::getSubTotal)
                .sum();
    }
}

