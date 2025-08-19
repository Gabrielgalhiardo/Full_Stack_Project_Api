package com.example.crud_em_aula_projeto.application.DTO;

import com.example.crud_em_aula_projeto.domain.entity.Sonho;
import com.example.crud_em_aula_projeto.domain.enuns.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SonhoDTO(
        @Schema(description = "ID do sonho", example = "2", hidden = true)
        Long id,

        @NotBlank(message = "Nome é obrigatorio")
        @Schema(description = "Nome do sonho", example = "Poupar dinheiro")
        String nome,

        @NotBlank(message = "Descrição é obrigatorio")
        @Schema(description = "Descrição do sonho", example = "Poupar dinheiro ate chegar a 1 milhao de reais")
        String descricao,

        @NotBlank(message = "Descrição é obrigatorio")
        @Schema(description = "Data final do sonho", example = "15/09/2025")
        String dataFinal,

        @NotBlank(message = "Status é obrigatorio")
        @Schema(description = "Status do sonho", example = "PENDENTE")
        Status status) {
    public static SonhoDTO toDTO(Sonho s) {
        return new SonhoDTO(s.getId(), s.getNome(), s.getDescricao(), s.getDataFinal(), s.getStatus());
    }

    public static Sonho fromDTO(SonhoDTO dto) {
        Sonho sonho = new Sonho();
        sonho.setId(dto.id());
        sonho.setNome(dto.nome());
        sonho.setDescricao(dto.descricao());
        sonho.setDataFinal(dto.dataFinal());
        sonho.setStatus(dto.status());
        return sonho;
    }
}
