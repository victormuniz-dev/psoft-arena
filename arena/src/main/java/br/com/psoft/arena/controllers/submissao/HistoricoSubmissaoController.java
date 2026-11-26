package br.com.psoft.arena.controllers.submissao;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.submissao.HistoricoSubmissaoResponseDto;
import br.com.psoft.arena.services.submissao.HistoricoSubmissaoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estudantes/{estudanteId}/historico-submissoes")
@RequiredArgsConstructor
public class HistoricoSubmissaoController {

    private final HistoricoSubmissaoService historicoService;

    @GetMapping
    public List<HistoricoSubmissaoResponseDto> listar(@PathVariable Long estudanteId) {
        return historicoService.listarHistorico(estudanteId);
    }

    @GetMapping("/{submissaoId}")
    public HistoricoSubmissaoResponseDto detalhar(
        @PathVariable Long estudanteId,
        @PathVariable Long submissaoId
    ) {
        return historicoService.buscarDetalhe(estudanteId, submissaoId);
    }
}