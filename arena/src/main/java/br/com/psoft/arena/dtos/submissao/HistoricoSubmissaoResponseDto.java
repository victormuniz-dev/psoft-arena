package br.com.psoft.arena.dtos.submissao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public record HistoricoSubmissaoResponseDto(
    Long submissaoId,
    Long atividadeId,
    String atividadeTitulo,
    Long turmaId,
    String turmaNome,
    LocalDateTime dataHoraSubmissao,
    Integer versao,
    StatusSubmissao status,
    String descricaoStatus,
    BigDecimal nota
) {

    public static HistoricoSubmissaoResponseDto of(
        SubmissaoModel submissao,
        AtividadeModel atividade,
        BigDecimal nota
    ) {
        return new HistoricoSubmissaoResponseDto(
            submissao.getId(),
            atividade.getId(),
            atividade.getTitulo(),
            atividade.getTurma().getId(),
            atividade.getTurma().getNome(),
            submissao.getDataHoraSubmissao(),
            submissao.getVersao(),
            submissao.getStatus(),
            submissao.getStatus().getDescricao(),
            nota
        );
    }
}