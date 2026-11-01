package br.com.psoft.arena.models.turma;

public enum StatusTurma {
    ATIVA,
    ENCERRADA;

    public StatusTurma alterarStatus(){
        return this == ATIVA ? ENCERRADA : ATIVA;
    }
}
