package br.com.psoft.arena.controllers.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.notificacao.NotificacaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.notificacao.NotificacaoRepository;
import br.com.psoft.arena.repositories.submissao.HistoricoStatusSubmissaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import br.com.psoft.arena.services.notificacao.NotificacaoService;
import br.com.psoft.arena.services.submissao.FilaSubmissao;
import br.com.psoft.arena.services.submissao.SubmissaoTransicaoService;
import br.com.psoft.arena.support.AvaliadorControlavel;
import br.com.psoft.arena.support.ValidadorSubmissaoPermissivoConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ValidadorSubmissaoPermissivoConfig.class)
class AvaliacaoIntegracaoPontaAPontaTest {

    private static final Duration TEMPO_MAXIMO = Duration.ofSeconds(20);
    private static final String CODIGO_FONTE = "public class Solution { public static void main(String[] args) {} }";
    private static final Long ATIVIDADE_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private AvaliadorControlavel avaliadorControlavel;

    @Autowired
    private FilaSubmissao filaSubmissao;

    @Autowired
    private SubmissaoTransicaoService submissaoTransicaoService;

    @Autowired
    private NotificacaoService notificacaoService;

    private EstudanteModel estudante;

    @BeforeEach
    void setUp() {
        avaliadorControlavel.reiniciar();

        notificacaoRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        historicoRepository.deleteAll();
        submissaoRepository.deleteAll();
        estudanteRepository.deleteAll();
        filaSubmissao.limpar();

        estudante = estudanteRepository.save(
            new EstudanteModel("Estudante US11", "2026110", "us11@arena.psoft.ufcg.edu.br")
        );
    }

    @AfterEach
    void tearDown() {
        avaliadorControlavel.liberarAvaliacoes();
        filaSubmissao.liberarDrenagem();
    }

    @Test
    void deveExecutarFluxoCompletoPontaAPontaUS11() throws Exception {
        // 1. Criar estudante (feito no setUp)

        // 2. Criar submissão via endpoint POST /submissoes
        MvcResult postResult = mockMvc.perform(post("/submissoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "estudanteId": %d,
                        "atividadeId": %d,
                        "codigoFonte": "%s"
                    }
                    """.formatted(estudante.getId(), ATIVIDADE_ID, CODIGO_FONTE)))
            .andExpect(status().isCreated())
            .andReturn();

        String location = postResult.getResponse().getHeader("Location");
        Long submissaoId = Long.valueOf(location.substring(location.lastIndexOf('/') + 1));

        // 3. Processar a submissão com avaliador controlado, persistir avaliação e concluir
        await().atMost(TEMPO_MAXIMO).pollInterval(Duration.ofMillis(100)).untilAsserted(() -> {
            SubmissaoModel submissao = submissaoRepository.findById(submissaoId).orElseThrow();
            assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.CONCLUIDA);
        });

        // 4. Confirmar que a avaliação foi persistida pelo AvaliadorControlavel
        AvaliacaoModel avaliacao = avaliacaoRepository.findBySubmissaoId(submissaoId).orElse(null);
        assertThat(avaliacao).isNotNull();
        assertThat(avaliacao.getNotaFinal().getNota()).isEqualByComparingTo(new BigDecimal("8.5"));
        assertThat(avaliacao.getNotaFinal().getJustificativa()).isNotBlank();
        assertThat(avaliacao.getNotaFinal().getEvidencias()).isNotBlank();

        // 5 & 6. Confirmar que a notificação foi gerada
        List<NotificacaoModel> notificacoes = notificacaoRepository
            .findByEstudanteIdOrderByDataHoraDesc(estudante.getId());
        assertThat(notificacoes).hasSize(1);
        NotificacaoModel notificacao = notificacoes.get(0);
        assertThat(notificacao.getEstudante().getId()).isEqualTo(estudante.getId());
        assertThat(notificacao.getSubmissao().getId()).isEqualTo(submissaoId);
        assertThat(notificacao.getMensagem()).contains("concluída");

        // 7. Consultar o resultado pelo endpoint GET /submissoes/estudante/{estudanteId}/{submissaoId}/resultado
        mockMvc.perform(get("/submissoes/estudante/{estudanteId}/{submissaoId}/resultado",
                estudante.getId(), submissaoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.submissaoId").value(submissaoId))
            .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
            .andExpect(jsonPath("$.disponivel").value(true))
            .andExpect(jsonPath("$.nota").value(8.5))
            .andExpect(jsonPath("$.justificativa").value(avaliacao.getNotaFinal().getJustificativa()))
            .andExpect(jsonPath("$.evidencias").value(avaliacao.getNotaFinal().getEvidencias()));

        // 8. Consultar as notificações pelo endpoint GET /notificacoes/estudante/{estudanteId}
        mockMvc.perform(get("/notificacoes/estudante/{estudanteId}", estudante.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].submissaoId").value(submissaoId))
            .andExpect(jsonPath("$[0].mensagem").value(notificacao.getMensagem()));
    }

    @Test
    void submissaoConcluidaSemAvaliacaoPersistidaNaoGeraNotificacao() {
        // Criar submissão diretamente no banco sem passar pelo fluxo completo
        SubmissaoModel submissao = submissaoRepository.save(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO_FONTE, 1)
        );
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();
        submissaoRepository.save(submissao);

        // Garantir que não há avaliação persistida
        assertThat(avaliacaoRepository.existsBySubmissaoId(submissao.getId())).isFalse();

        // Tentar notificar deve falhar com exceção de regra de negócio
        org.junit.jupiter.api.Assertions.assertThrows(
            br.com.psoft.arena.exception.BusinessRuleException.class,
            () -> notificacaoService.notificarResultadoDisponivel(submissao)
        );

        // Nenhuma notificação deve ter sido gerada
        assertThat(notificacaoRepository.findBySubmissaoId(submissao.getId())).isEmpty();
    }

    @Test
    void naoDevePermitirEstudanteConsultarResultadoDeOutroEstudante() throws Exception {
        // Criar submissão para estudante 1
        SubmissaoModel submissao = submissaoRepository.save(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO_FONTE, 1)
        );
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();
        submissaoRepository.save(submissao);

        // Criar avaliação
        avaliacaoRepository.save(new AvaliacaoModel(
            submissao,
            new br.com.psoft.arena.models.avaliacao.NotaFinal(
                new BigDecimal("9.0"), "Evidências", "Justificativa"
            ),
            List.of()
        ));

        // Outro estudante
        EstudanteModel outroEstudante = estudanteRepository.save(
            new EstudanteModel("Outro Estudante", "2026111", "outro@arena.psoft.ufcg.edu.br")
        );

        // Consulta pelo outro estudante deve retornar 404 (não encontrada para aquele estudante)
        mockMvc.perform(get("/submissoes/estudante/{estudanteId}/{submissaoId}/resultado",
                outroEstudante.getId(), submissao.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void consultaDeNotificacoesDeEstudanteDiferenteNaoRetornaNotificacoesAlheias() throws Exception {
        SubmissaoModel submissao = submissaoRepository.save(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO_FONTE, 1)
        );
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();
        submissaoRepository.save(submissao);

        avaliacaoRepository.save(new AvaliacaoModel(
            submissao,
            new br.com.psoft.arena.models.avaliacao.NotaFinal(
                new BigDecimal("9.0"), "Evidências", "Justificativa"
            ),
            List.of()
        ));

        notificacaoService.notificarResultadoDisponivel(submissao);

        EstudanteModel outroEstudante = estudanteRepository.save(
            new EstudanteModel("Estudante Sem Notif", "2026112", "semnotif@arena.psoft.ufcg.edu.br")
        );

        mockMvc.perform(get("/notificacoes/estudante/{estudanteId}", outroEstudante.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}
