package br.com.psoft.arena.services.submissao;

import org.springframework.stereotype.Component;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.submissao.SubmissaoModel;

@Component
public class AvaliadorSubmissaoPadrao implements AvaliadorSubmissao {

    @Override
    public void avaliar(SubmissaoModel submissao) {
        if (submissao.getCodigoFonte() == null || submissao.getCodigoFonte().isBlank()) {
            throw new BusinessRuleException(
                "A submissão não possui código-fonte para ser avaliado."
            );
        }
    }
}
