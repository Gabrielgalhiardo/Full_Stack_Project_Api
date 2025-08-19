package com.example.crud_em_aula_projeto.controller.exception;

import com.example.crud_em_aula_projeto.domain.exception.IdNaoEncontrado;
import com.example.crud_em_aula_projeto.domain.exception.MuitosSonhosException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class TaskControllerAdivce {

    @ExceptionHandler(MuitosSonhosException.class)
    public ResponseEntity<Map<String, Object>> handleMuitosSonhos(MuitosSonhosException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(IdNaoEncontrado.class)
    public ResponseEntity<Map<String, Object>> handleIdNaoEncontrado(IdNaoEncontrado ex){
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.BAD_REQUEST.value());
        errorBody.put("error", "Bad Request");
        errorBody.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }
}
