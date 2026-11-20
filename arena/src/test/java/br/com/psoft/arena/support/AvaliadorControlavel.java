package br.com.psoft.arena.support;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.services.submissao.AvaliadorSubmissao;

@Primary
@Component
public class AvaliadorControlavel implements AvaliadorSubmissao {

    private static final String MARCADOR_FALHA = "AVALIACAO_FALHA";

    private final AvaliacaoRepository avaliacaoRepository;
    private final AtomicReference<String> motivoFalha = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> portao = new AtomicReference<>();

    public AvaliadorControlavel(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public void falharProximaAvaliacaoCom(String motivo) {
        motivoFalha.set(motivo);
    }

    public CountDownLatch segurarAvaliacoes() {
        CountDownLatch novoPortao = new CountDownLatch(1);
        portao.set(novoPortao);

        return novoPortao;
    }

    public void liberarAvaliacoes() {
        CountDownLatch atual = portao.getAndSet(null);

        if (atual != null) {
            atual.countDown();
        }
    }

    public void reiniciar() {
        motivoFalha.set(null);
        liberarAvaliacoes();
    }

    @Override
    public void avaliar(SubmissaoModel submissao) {
        aguardarPortao();

        String motivo = motivoFalha.getAndSet(null);

        if (motivo != null) {
            throw new BusinessRuleException(motivo);
        }

        if (submissao.getCodigoFonte() != null
            && submissao.getCodigoFonte().contains(MARCADOR_FALHA)) {
            throw new BusinessRuleException("O avaliador detectou código inválido.");
        }

        // Cenário de sucesso: persistir avaliação determinística
        if (!avaliacaoRepository.existsBySubmissaoId(submissao.getId())) {
            NotaFinal notaFinal = new NotaFinal(
                new BigDecimal("8.5"),
                "Código limpo e testes executados com sucesso.",
                "Implementação atende integralmente aos requisitos da atividade."
            );
            AvaliacaoModel avaliacao = new AvaliacaoModel(submissao, notaFinal, List.of());
            avaliacaoRepository.save(avaliacao);
        }
    }

    private void aguardarPortao() {
        CountDownLatch atual = portao.get();

        if (atual == null) {
            return;
        }

        try {
            if (!atual.await(30, TimeUnit.SECONDS)) {
                throw new BusinessRuleException("Tempo esgotado aguardando a liberação do avaliador.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessRuleException("Avaliação interrompida.");
        }
    }
}
