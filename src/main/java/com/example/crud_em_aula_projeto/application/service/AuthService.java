package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.AuthDTO;
import com.example.crud_em_aula_projeto.domain.model.entity.Usuario;
import com.example.crud_em_aula_projeto.domain.repository.UsuarioRepository;
import com.example.crud_em_aula_projeto.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public String login(AuthDTO.LoginRequest req) {
        Usuario usuario = usuarios.findByEmail(req.email())
                .orElseThrow(() ->  new RuntimeException("Usuário não encontrado"));

        if (!encoder.matches(req.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        return jwt.generateToken(usuario.getEmail(), usuario.getRole().name());
    }
}