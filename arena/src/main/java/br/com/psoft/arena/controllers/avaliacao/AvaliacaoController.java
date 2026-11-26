package br.com.psoft.arena.controllers.avaliacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.avaliacao.ResultadoAvaliacaoResponseDto;
import br.com.psoft.arena.services.avaliacao.AvaliacaoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/submissoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping("/estudante/{estudanteId}/{submissaoId}/resultado")
    public ResponseEntity<ResultadoAvaliacaoResponseDto> consultarResultado(
        @PathVariable Long estudanteId,
        @PathVariable Long submissaoId
    ) {
        return ResponseEntity.ok(
            avaliacaoService.consultarResultado(estudanteId, submissaoId)
        );
    }
}
