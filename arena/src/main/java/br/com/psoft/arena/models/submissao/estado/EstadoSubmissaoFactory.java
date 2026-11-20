package br.com.psoft.arena.models.submissao.estado;

import java.util.Map;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.submissao.StatusSubmissao;

public final class EstadoSubmissaoFactory {

    private static final Map<StatusSubmissao, EstadoSubmissao> ESTADOS = Map.of(
        StatusSubmissao.SUBMETIDA, new EstadoSubmetida(),
        StatusSubmissao.EM_FILA, new EstadoEmFila(),
        StatusSubmissao.PROCESSANDO, new EstadoProcessando(),
        StatusSubmissao.CONCLUIDA, new EstadoConcluida(),
        StatusSubmissao.FALHA, new EstadoFalha()
    );

    private EstadoSubmissaoFactory() {
    }

    public static EstadoSubmissao obter(StatusSubmissao status) {
        if (status == null) {
            throw new BusinessRuleException("O estado da submissão deve ser informado.");
        }

        EstadoSubmissao estado = ESTADOS.get(status);

        if (estado == null) {
            throw new BusinessRuleException("Estado de submissão desconhecido: " + status + ".");
        }

        return estado;
    }
}
