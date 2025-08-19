package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.DTO.SonhoDTO;
import com.example.crud_em_aula_projeto.domain.exception.IdNaoEncontrado;
import com.example.crud_em_aula_projeto.infrastructure.repository.SonhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SonhoService {
    private final SonhoRepository sonhoRepository;

    public SonhoService(SonhoRepository sonhoRepository) {
        this.sonhoRepository = sonhoRepository;
    }

    public SonhoDTO getSonhoById(Long id) {
        return sonhoRepository.findById(id)
                .map(SonhoDTO::toDTO)
                .orElseThrow(() -> new IdNaoEncontrado("Sonho nao encontrado com o id: " + id));
    }

    public List<SonhoDTO> getAllSonhos() {
        return sonhoRepository.findAll().stream().map(
            SonhoDTO::toDTO
        ).toList();
    }

    public SonhoDTO updateSonho(Long id, SonhoDTO sonhoDTONew){
        return sonhoRepository.findById(id).map(
            sonho -> {
                sonho.setNome(sonhoDTONew.nome());
                sonho.setDescricao(sonhoDTONew.descricao());
                sonho.setDataFinal(sonhoDTONew.dataFinal());
                sonho.setStatus(sonhoDTONew.status());
                return SonhoDTO.toDTO(sonhoRepository.save(sonho));
            }
        ).orElseThrow(() -> new IdNaoEncontrado("Sonho não encontrado com o id: " + id));

    }

    public void delete (Long id){
        sonhoRepository.deleteById(id);
    }

}
