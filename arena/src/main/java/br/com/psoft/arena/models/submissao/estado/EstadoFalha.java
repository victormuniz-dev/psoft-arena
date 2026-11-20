package br.com.psoft.arena.models.submissao.estado;

import br.com.psoft.arena.models.submissao.StatusSubmissao;

public class EstadoFalha implements EstadoSubmissao {

    @Override
    public StatusSubmissao getStatus() {
        return StatusSubmissao.FALHA;
    }
}
