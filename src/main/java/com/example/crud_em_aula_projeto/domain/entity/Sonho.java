package com.example.crud_em_aula_projeto.domain.entity;

import com.example.crud_em_aula_projeto.domain.enuns.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sonhos")

public class Sonho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nome;
    String descricao;
    String dataFinal;

    @Enumerated(EnumType.STRING)
    Status status;
}
