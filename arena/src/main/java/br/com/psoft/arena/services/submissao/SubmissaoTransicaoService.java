package br.com.psoft.arena.services.submissao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import br.com.psoft.arena.services.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissaoTransicaoService {

    private final SubmissaoRepository submissaoRepository;
    private final NotificacaoService notificacaoService;

    @Transactional
    public SubmissaoModel encaminharParaFila(Long submissaoId) {
        SubmissaoModel submissao = buscar(submissaoId);
        submissao.encaminharParaFila();

        return submissao;
    }

    @Transactional
    public SubmissaoModel iniciarProcessamento(Long submissaoId) {
        SubmissaoModel submissao = buscar(submissaoId);
        submissao.iniciarProcessamento();

        return submissao;
    }

    @Transactional
    public SubmissaoModel concluir(Long submissaoId) {
        SubmissaoModel submissao = buscar(submissaoId);
        submissao.concluir();
        notificacaoService.notificarResultadoDisponivel(submissao);

        return submissao;
    }

    @Transactional
    public SubmissaoModel falhar(Long submissaoId, String motivo) {
        SubmissaoModel submissao = buscar(submissaoId);
        submissao.falhar(motivo);

        return submissao;
    }

    @Transactional(readOnly = true)
    public SubmissaoModel buscarDoEstudante(Long submissaoId, Long estudanteId) {
        return submissaoRepository.findByIdAndEstudanteId(submissaoId, estudanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));
    }

    private SubmissaoModel buscar(Long submissaoId) {
        return submissaoRepository.findComHistoricoById(submissaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));
    }
}
