package br.com.psoft.arena.services.submissao;

import org.springframework.stereotype.Component;

@Component
public class ValidadorSubmissaoPadrao implements ValidadorSubmissao {

    @Override
    public void validar(Long estudanteId, Long atividadeId) {
    }
}
