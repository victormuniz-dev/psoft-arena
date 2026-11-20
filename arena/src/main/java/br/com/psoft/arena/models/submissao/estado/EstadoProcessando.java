package br.com.psoft.arena.models.submissao.estado;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public class EstadoProcessando implements EstadoSubmissao {

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.PROCESSANDO;
    }

    @Override
    public void concluir(SubmissaoModel submissao) {
        submissao.transicionarPara(
            StatusSubmissao.CONCLUIDA,
            "Avaliação da submissão concluída com sucesso."
        );
    }

    @Override
    public void falhar(SubmissaoModel submissao, String motivo) {
        String observacao = motivo == null || motivo.isBlank()
            ? "Ocorreu um erro que impediu a conclusão da avaliação."
            : motivo;

        submissao.transicionarPara(StatusSubmissao.FALHA, observacao);
    }
}
