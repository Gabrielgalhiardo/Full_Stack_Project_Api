package com.example.crud_em_aula_projeto.domain.model.entity;

import com.example.crud_em_aula_projeto.domain.model.enuns.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @NotBlank
    @Column(nullable = false)
    protected String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    protected String email;

    @NotBlank
    @Column(nullable = false)
    protected String passwordHash;

    @Column(nullable = false)
    protected Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    protected Role role;

}
