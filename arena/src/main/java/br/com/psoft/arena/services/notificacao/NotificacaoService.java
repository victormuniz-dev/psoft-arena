package br.com.psoft.arena.services.notificacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.notificacao.NotificacaoResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.notificacao.NotificacaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.notificacao.NotificacaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final EstudanteRepository estudanteRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional
    public NotificacaoModel notificarResultadoDisponivel(
        SubmissaoModel submissao
    ) {
        if (submissao == null
            || submissao.getStatus() != StatusSubmissao.CONCLUIDA) {
            throw new BusinessRuleException(
                "Somente submissões concluídas podem gerar notificação de resultado."
            );
        }

        if (!avaliacaoRepository.existsBySubmissaoId(submissao.getId())) {
            throw new BusinessRuleException(
                "Não é possível gerar notificação para submissão sem avaliação persistida."
            );
        }

        return notificacaoRepository.findBySubmissaoId(submissao.getId())
            .orElseGet(() -> {
                String mensagem = "Sua avaliação da submissão #"
                    + submissao.getId()
                    + " (versão " + submissao.getVersao()
                    + ") da atividade #" + submissao.getAtividadeId()
                    + " foi concluída. O resultado está disponível para consulta.";

                NotificacaoModel notificacao = new NotificacaoModel(
                    submissao.getEstudante(),
                    submissao,
                    mensagem
                );

                return notificacaoRepository.save(notificacao);
            });
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDto> listarPorEstudante(Long estudanteId) {
        if (estudanteId == null
            || !estudanteRepository.existsById(estudanteId)) {
            throw new ResourceNotFoundException("Estudante", estudanteId);
        }

        return notificacaoRepository
            .findByEstudanteIdOrderByDataHoraDesc(estudanteId)
            .stream()
            .map(NotificacaoResponseDto::from)
            .toList();
    }
}
