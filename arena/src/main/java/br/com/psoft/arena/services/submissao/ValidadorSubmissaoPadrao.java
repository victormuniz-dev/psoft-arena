package br.com.psoft.arena.services.submissao;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidadorSubmissaoPadrao implements ValidadorSubmissao {

    private final AtividadeRepository atividadeRepository;

    @Override
    @Transactional(readOnly = true)
    public void validar(Long estudanteId, Long atividadeId) {
        AtividadeModel atividade = atividadeRepository.findById(atividadeId)
            .orElseThrow(() -> new ResourceNotFoundException("Atividade", atividadeId));

        if (!atividade.getTurma().contemEstudante(estudanteId)) {
            throw new BusinessRuleException("O estudante não pertence à turma desta atividade.");
        }

        atividade.garantirAceitaSubmissoes(LocalDateTime.now());
    }
}