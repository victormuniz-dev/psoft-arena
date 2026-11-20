package br.com.psoft.arena.models.submissao.estado;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public class EstadoSubmetida implements EstadoSubmissao {

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.SUBMETIDA;
    }

    @Override
    public void encaminharParaFila(SubmissaoModel submissao) {
        submissao.transicionarPara(
            StatusSubmissao.EM_FILA,
            "Submissão encaminhada para a fila de processamento."
        );
    }
}
