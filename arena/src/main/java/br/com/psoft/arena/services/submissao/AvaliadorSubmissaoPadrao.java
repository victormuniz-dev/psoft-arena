package br.com.psoft.arena.services.submissao;

import org.springframework.stereotype.Component;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.services.avaliacao.AvaliacaoService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AvaliadorSubmissaoPadrao implements AvaliadorSubmissao {

    private final AvaliacaoService avaliacaoService;

    @Override
    public void avaliar(SubmissaoModel submissao) {
        if (submissao.getCodigoFonte() == null || submissao.getCodigoFonte().isBlank()) {
            throw new BusinessRuleException(
                "A submissão não possui código-fonte para ser avaliado."
            );
        }

        avaliacaoService.avaliar(submissao.getId());
    }
}
