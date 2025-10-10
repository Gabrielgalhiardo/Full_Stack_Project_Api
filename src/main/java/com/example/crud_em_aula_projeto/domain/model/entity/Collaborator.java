package com.example.crud_em_aula_projeto.domain.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "collaborators")
public class Collaborator extends Usuario {

    @OneToMany(mappedBy = "collaborator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;


}
