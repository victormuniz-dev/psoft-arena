package br.com.psoft.arena.models.submissao.estado;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public class EstadoEmFila implements EstadoSubmissao {

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.EM_FILA;
    }

    @Override
    public void iniciarProcessamento(SubmissaoModel submissao) {
        submissao.transicionarPara(
            StatusSubmissao.PROCESSANDO,
            "Processamento da submissão iniciado."
        );
    }

    @Override
    public void falhar(SubmissaoModel submissao, String motivo) {
        String observacao = motivo == null || motivo.isBlank()
            ? "A submissão não pôde ser processada."
            : motivo;

        submissao.transicionarPara(StatusSubmissao.FALHA, observacao);
    }
}
