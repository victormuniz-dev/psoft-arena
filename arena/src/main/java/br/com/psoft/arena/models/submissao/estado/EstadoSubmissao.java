package br.com.psoft.arena.models.submissao.estado;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

public interface EstadoSubmissao {

    StatusSubmissao getStatus();

    default void encaminharParaFila(SubmissaoModel submissao) {
        throw transicaoInvalida(submissao, "encaminhar para a fila de processamento");
    }

    default void iniciarProcessamento(SubmissaoModel submissao) {
        throw transicaoInvalida(submissao, "iniciar o processamento");
    }

    default void concluir(SubmissaoModel submissao) {
        throw transicaoInvalida(submissao, "concluir");
    }

    default void falhar(SubmissaoModel submissao, String motivo) {
        throw transicaoInvalida(submissao, "registrar falha");
    }

    default BusinessRuleException transicaoInvalida(SubmissaoModel submissao, String acao) {
        return new BusinessRuleException(
            "Não é possível " + acao + " a submissão "
                + identificacao(submissao)
                + ", pois ela está no estado " + getStatus().getDescricao() + "."
        );
    }

    default String identificacao(SubmissaoModel submissao) {
        if (submissao == null || submissao.getId() == null) {
            return "ainda não persistida";
        }

        return "#" + submissao.getId();
    }
}
