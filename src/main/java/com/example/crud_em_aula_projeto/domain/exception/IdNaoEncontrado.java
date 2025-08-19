package com.example.crud_em_aula_projeto.domain.exception;

public class IdNaoEncontrado extends RuntimeException {
    public IdNaoEncontrado(String message) {
        super(message);
    }
}
