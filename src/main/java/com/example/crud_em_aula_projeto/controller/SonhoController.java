package com.example.crud_em_aula_projeto.controller;

import com.example.crud_em_aula_projeto.application.DTO.SonhoDTO;
import com.example.crud_em_aula_projeto.application.service.SonhoService;
import com.example.crud_em_aula_projeto.domain.enuns.Status;
import com.example.crud_em_aula_projeto.domain.service.SonhoServiceDomain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sonhos", description = "Gerenciamento de Sonhos")
@RestController
@RequestMapping("/sonhos")

public class SonhoController {

    private final SonhoServiceDomain sonhoServiceDomain;
    private final SonhoService sonhoService;

    public SonhoController(SonhoServiceDomain sonhoServiceDomain, SonhoService sonhoService) {
        this.sonhoServiceDomain = sonhoServiceDomain;
        this.sonhoService = sonhoService;
    }

    @Operation(
            summary = "Listar todos os sonhos",
            description = "Retorna todos os sonhos cadastrados"
    )
    @GetMapping()
    public ResponseEntity<List<SonhoDTO>> getAllSonhos() {
        return ResponseEntity.ok().body(sonhoService.getAllSonhos());
    }

    @Operation(
            summary = "Buscar Sonho por ID",
            description = "Retorna um sonho a partir do seu ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sonho encontrado"),
                    @ApiResponse(responseCode = "404", description = "Sonho nao encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SonhoDTO> getSonhoById(@PathVariable Long id) {
        return ResponseEntity.ok().body(sonhoService.getSonhoById(id));
    }

    @Operation(
            summary = "Atualizar um serviço",
            description = "Atualiza os dados de um sonho existente com novas informações",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SonhoDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sonho atualizado"),
                    @ApiResponse(responseCode = "400", description = "Violação de regras de negócio")
            }
    )
    @PutMapping("/update/{id}")
    public ResponseEntity<SonhoDTO> updateSonho(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody SonhoDTO sonhoDTONew) {
        return ResponseEntity.ok().body(sonhoService.updateSonho(id, sonhoDTONew));
    }

    @Operation(
            summary = "Deletar um Sonho",
            description = "Remove um sonho da base de dados a partir do seu ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Sonho removido com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Sonho não encontrado")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSonho(@PathVariable Long id) {
        sonhoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cadastrar um novo sonho",
            description = "Adiciona um novo sonho a base de dados apos validação de ter menos de 5 sonhos pendentes",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SonhoDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "nome": "Ganhar na megasdsssssssa",
                                      "descricao": "Quero aprender a voar",
                                      "dataFinal": "2023-12-31",
                                      "status": "PENDENTE"
                                    }
                                    """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sonho cadastrado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Violação de regras de negócio")
            }
    )
    @PostMapping()
    public ResponseEntity<SonhoDTO> saveSonho(@Valid @org.springframework.web.bind.annotation.RequestBody SonhoDTO sonhoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sonhoServiceDomain.saveSonho(sonhoDTO));
    }

    @Operation(
            summary = "Pegar sonhos por status",
            description = "Retorna uma lista de sonhos existente a partir de seus status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Sonho encontrado"),
                    @ApiResponse(responseCode = "404", description = "Sonho não encontrado")
            }
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SonhoDTO>> getAllSonhosByStatus(@PathVariable String status) {
        return ResponseEntity.ok().body(sonhoServiceDomain.getAllSonhosByStatus(Status.valueOf(status.toUpperCase())));
    }


}
