package com.example.crud_em_aula_projeto.domain.repository;

import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {
    Optional<Collaborator> findByEmail(String email);
    List<Collaborator> findAllByActiveTrue();
}
