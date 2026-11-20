package br.com.psoft.arena.dtos.avaliacao;

import java.util.List;

import br.com.psoft.arena.dtos.resultado.ResultadoLLMResponseDto;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;

public record AvaliacaoResponseDto(
        Long id,
        Long submissaoId,
        double nota,
        String evidencias,
        String justificativa,
        List<ResultadoLLMResponseDto> resultados) {

    public static AvaliacaoResponseDto de(AvaliacaoModel avaliacao) {
        return new AvaliacaoResponseDto(
                avaliacao.getId(),
                avaliacao.getSubmissao().getId(),
                avaliacao.getNotaFinal().getNota() != null ? avaliacao.getNotaFinal().getNota().doubleValue() : 0.0,
                avaliacao.getNotaFinal().getEvidencias(),
                avaliacao.getNotaFinal().getJustificativa(),
                avaliacao.getResultados().stream()
                        .map(ResultadoLLMResponseDto::de)
                        .toList());
    }
}