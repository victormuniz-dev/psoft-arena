package br.com.psoft.arena.dtos.avaliacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.psoft.arena.dtos.resultado.ResultadoLLMResponseDto;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public record ResultadoAvaliacaoResponseDto(
    Long submissaoId,
    StatusSubmissao situacao,
    boolean disponivel,
    String mensagem,
    LocalDateTime dataAvaliacao,
    BigDecimal nota,
    String justificativa,
    String evidencias,
    List<ResultadoLLMResponseDto> resultados
) {

    public static ResultadoAvaliacaoResponseDto indisponivel(SubmissaoModel submissao) {
        String mensagem = submissao.getStatus() == StatusSubmissao.FALHA
            ? "A avaliação falhou e o resultado ainda não está disponível."
            : "A avaliação ainda não foi concluída.";

        return new ResultadoAvaliacaoResponseDto(
            submissao.getId(),
            submissao.getStatus(),
            false,
            mensagem,
            null,
            null,
            null,
            null,
            List.of()
        );
    }

    public static ResultadoAvaliacaoResponseDto disponivel(AvaliacaoModel avaliacao) {
        return new ResultadoAvaliacaoResponseDto(
            avaliacao.getSubmissao().getId(),
            avaliacao.getSubmissao().getStatus(),
            true,
            "Resultado da avaliação disponível.",
            avaliacao.getDataAvaliacao(),
            avaliacao.getNotaFinal().getNota(),
            avaliacao.getNotaFinal().getJustificativa(),
            avaliacao.getNotaFinal().getEvidencias(),
            avaliacao.getResultados().stream()
                .map(ResultadoLLMResponseDto::de)
                .toList()
        );
    }
}
