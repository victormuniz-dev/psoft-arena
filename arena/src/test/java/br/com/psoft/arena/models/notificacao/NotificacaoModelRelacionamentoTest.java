package br.com.psoft.arena.models.notificacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.notificacao.NotificacaoRepository;
import br.com.psoft.arena.repositories.submissao.HistoricoStatusSubmissaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;

@SpringBootTest
class NotificacaoModelRelacionamentoTest {

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private SubmissaoRepository submissaoRepository;

    @Autowired
    private HistoricoStatusSubmissaoRepository historicoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private EstudanteModel estudante;
    private SubmissaoModel submissao;

    @BeforeEach
    void setUp() {
        notificacaoRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        historicoRepository.deleteAll();
        submissaoRepository.deleteAll();
        estudanteRepository.deleteAll();

        estudante = estudanteRepository.save(
            new EstudanteModel("Estudante Relacionamento", "2026100", "rel@email.com")
        );

        submissao = submissaoRepository.save(
            new SubmissaoModel(estudante, 10L, "public class Rel {}", 1)
        );
    }

    @Test
    void exclusaoDeNotificacaoNaoPodeExcluirSubmissao() {
        NotificacaoModel notificacao = notificacaoRepository.save(
            new NotificacaoModel(estudante, submissao, "Notificação de teste")
        );

        Long notifId = notificacao.getId();
        Long submissaoId = submissao.getId();

        assertThat(notificacaoRepository.existsById(notifId)).isTrue();
        assertThat(submissaoRepository.existsById(submissaoId)).isTrue();

        // Remove a notificação
        notificacaoRepository.deleteById(notifId);
        notificacaoRepository.flush();

        // A notificação não existe mais, mas a submissão DEVE continuar existindo
        assertThat(notificacaoRepository.existsById(notifId)).isFalse();
        assertThat(submissaoRepository.existsById(submissaoId)).isTrue();
    }

    @Test
    void exclusaoDeSubmissaoDeveRemoverNotificacaoDependente() {
        NotificacaoModel notificacao = notificacaoRepository.save(
            new NotificacaoModel(estudante, submissao, "Notificação dependente")
        );

        Long notifId = notificacao.getId();
        Long submissaoId = submissao.getId();

        assertThat(notificacaoRepository.existsById(notifId)).isTrue();
        assertThat(submissaoRepository.existsById(submissaoId)).isTrue();

        // Remove a submissão (primeiro o histórico que tem FK sem onDelete cascade)
        historicoRepository.deleteAll(historicoRepository.findBySubmissaoIdOrderByDataHoraAscIdAsc(submissaoId));
        submissaoRepository.deleteById(submissaoId);
        submissaoRepository.flush();

        // Pelo OnDelete(CASCADE) configurado na NotificacaoModel no banco H2, a notificação deve ser removida
        assertThat(submissaoRepository.existsById(submissaoId)).isFalse();
        assertThat(notificacaoRepository.existsById(notifId)).isFalse();
    }
}
