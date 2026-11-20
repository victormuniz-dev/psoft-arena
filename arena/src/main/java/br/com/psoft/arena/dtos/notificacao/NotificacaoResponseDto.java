package br.com.psoft.arena.dtos.notificacao;

import java.time.LocalDateTime;

import br.com.psoft.arena.models.notificacao.NotificacaoModel;

public record NotificacaoResponseDto(
    Long id,
    Long submissaoId,
    String mensagem,
    LocalDateTime dataHora
) {

    public static NotificacaoResponseDto from(NotificacaoModel notificacao) {
        return new NotificacaoResponseDto(
            notificacao.getId(),
            notificacao.getSubmissao().getId(),
            notificacao.getMensagem(),
            notificacao.getDataHora()
        );
    }
}
