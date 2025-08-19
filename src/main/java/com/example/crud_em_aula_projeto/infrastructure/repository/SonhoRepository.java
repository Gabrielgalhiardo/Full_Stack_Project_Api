package com.example.crud_em_aula_projeto.infrastructure.repository;

import com.example.crud_em_aula_projeto.domain.entity.Sonho;
import com.example.crud_em_aula_projeto.domain.enuns.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SonhoRepository extends JpaRepository<Sonho, Long> {
    List<Sonho> findBystatus(Status status);

}
