package com.example.crud_em_aula_projeto.domain.service;

import com.example.crud_em_aula_projeto.application.DTO.SonhoDTO;
import com.example.crud_em_aula_projeto.domain.enuns.Status;
import com.example.crud_em_aula_projeto.domain.exception.MuitosSonhosException;
import com.example.crud_em_aula_projeto.infrastructure.repository.SonhoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SonhoServiceDomain {
    private final SonhoRepository sonhoRepository;

    public SonhoDTO saveSonho(SonhoDTO sonhoDTO) {
        if(getAllSonhosByStatus(Status.PENDENTE).size() >5){ //so pode adicionar 5 com status pendente, evita ter muito sonhos
            throw new MuitosSonhosException("Muitos sonhos, complete eles primeiro");
        }else{
            sonhoRepository.save(SonhoDTO.fromDTO(sonhoDTO));
        }

        return sonhoDTO;
    }

    public SonhoServiceDomain(SonhoRepository sonhoRepository) {
        this.sonhoRepository = sonhoRepository;
    }

    public List<SonhoDTO> getAllSonhosByStatus(Status status){
        return sonhoRepository.findBystatus(status)
                .stream()
                .map(SonhoDTO::toDTO).toList();
    }



}
