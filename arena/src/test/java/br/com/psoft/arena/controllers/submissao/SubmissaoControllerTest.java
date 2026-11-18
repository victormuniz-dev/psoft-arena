package br.com.psoft.arena.controllers.submissao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.HistoricoStatusSubmissaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.submissao.HistoricoStatusSubmissaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import br.com.psoft.arena.services.submissao.FilaSubmissao;
import br.com.psoft.arena.services.submissao.SubmissaoService;
import br.com.psoft.arena.support.AvaliadorControlavel;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissaoControllerTest {

    private static final Duration TEMPO_MAXIMO = Duration.ofSeconds(20);
    private static final String CODIGO = "public class Main { }";
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
    private SubmissaoService submissaoService;

    @Autowired
    private FilaSubmissao filaSubmissao;

    @Autowired
    private AvaliadorControlavel avaliadorControlavel;

    @Autowired
    private ThreadPoolTaskExecutor submissaoExecutor;

    private EstudanteModel estudante;

    @BeforeEach
    void prepararBanco() {
        avaliadorControlavel.reiniciar();
        aguardarFilaOciosa();

        historicoRepository.deleteAll();
        submissaoRepository.deleteAll();
        estudanteRepository.deleteAll();
        filaSubmissao.limpar();

        estudante = estudanteRepository.save(
            new EstudanteModel("Ana Silva", "2026001", "ana@example.com")
        );
    }

    @AfterEach
    void limparExecucaoAssincrona() {
        avaliadorControlavel.liberarAvaliacoes();
        filaSubmissao.liberarDrenagem();
        submissaoExecutor.getThreadPoolExecutor().getQueue().clear();
    }

    @Test
    void deveProcessarSubmissaoAssincronamentePercorrendoTodosOsEstados() throws Exception {
        Long submissaoId = submeterERetornarId(estudante.getId(), ATIVIDADE_ID, CODIGO);

        aguardarStatus(submissaoId, StatusSubmissao.CONCLUIDA);

        assertThat(historicoDoBanco(submissaoId))
            .extracting(HistoricoStatusSubmissaoModel::getStatus)
            .containsExactly(
                StatusSubmissao.SUBMETIDA,
                StatusSubmissao.EM_FILA,
                StatusSubmissao.PROCESSANDO,
                StatusSubmissao.CONCLUIDA
            );

        mockMvc.perform(get("/submissoes/estudante/{estudanteId}", estudante.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(submissaoId))
            .andExpect(jsonPath("$[0].status").value("CONCLUIDA"))
            .andExpect(jsonPath("$[0].descricaoStatus").value("Concluída"));

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}",
                estudante.getId(),
                submissaoId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigoFonte").value(CODIGO))
            .andExpect(jsonPath("$.historico.length()").value(4))
            .andExpect(jsonPath("$.historico[0].status").value("SUBMETIDA"))
            .andExpect(jsonPath("$.historico[1].status").value("EM_FILA"))
            .andExpect(jsonPath("$.historico[2].status").value("PROCESSANDO"))
            .andExpect(jsonPath("$.historico[3].status").value("CONCLUIDA"));

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}/historico",
                estudante.getId(),
                submissaoId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].status").value("SUBMETIDA"))
            .andExpect(jsonPath("$[3].status").value("CONCLUIDA"));
    }

    @Test
    void deveRegistrarFalhaEOMotivoNoHistoricoQuandoAvaliacaoFalha() throws Exception {
        String motivo = "A API do modelo respondeu com erro 503.";
        String motivoPersistido = "Falha na avaliação: " + motivo;
        avaliadorControlavel.falharProximaAvaliacaoCom(motivo);

        Long submissaoId = submeterERetornarId(estudante.getId(), ATIVIDADE_ID, CODIGO);

        aguardarStatus(submissaoId, StatusSubmissao.FALHA);

        assertThat(historicoDoBanco(submissaoId))
            .extracting(HistoricoStatusSubmissaoModel::getStatus)
            .containsExactly(
                StatusSubmissao.SUBMETIDA,
                StatusSubmissao.EM_FILA,
                StatusSubmissao.PROCESSANDO,
                StatusSubmissao.FALHA
            );

        assertThat(historicoDoBanco(submissaoId).get(3).getObservacao())
            .contains(motivo);

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}",
                estudante.getId(),
                submissaoId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FALHA"))
            .andExpect(jsonPath("$.descricaoStatus").value("Falha"))
            .andExpect(jsonPath("$.historico.length()").value(4))
            .andExpect(jsonPath("$.historico[3].status").value("FALHA"))
            .andExpect(jsonPath("$.historico[3].observacao").value(motivoPersistido));

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}/historico",
                estudante.getId(),
                submissaoId
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[3].status").value("FALHA"))
            .andExpect(jsonPath("$[3].observacao").value(motivoPersistido));
    }

    @Test
    void deveMarcarFalhaComMotivoDescritivoQuandoOFilaDoExecutorRejeitaATarefa() {
        aguardarFilaOciosa();

        var executor = submissaoExecutor.getThreadPoolExecutor();
        var filaDoExecutor = executor.getQueue();
        int capacidadeRestante = filaDoExecutor.remainingCapacity();

        assertThat(capacidadeRestante).isPositive();

        CountDownLatch portao = avaliadorControlavel.segurarAvaliacoes();
        Long submissaoBloqueadaId = criarSubmissaoSemEncaminhar();

        submissaoService.encaminharParaProcessamento(submissaoBloqueadaId);
        aguardarStatus(submissaoBloqueadaId, StatusSubmissao.PROCESSANDO);

        assertThat(filaSubmissao.estaEmDrenagem()).isTrue();

        for (int indice = 0; indice < capacidadeRestante; indice++) {
            filaDoExecutor.add(new TarefaBloqueante(portao));
        }

        assertThat(filaDoExecutor.remainingCapacity()).isZero();

        filaSubmissao.liberarDrenagem();

        Long submissaoRejeitadaId = criarSubmissaoSemEncaminhar();

        assertThatThrownBy(() -> submissaoService.encaminharParaProcessamento(submissaoRejeitadaId))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("fila de processamento de submissões está cheia");

        assertThat(filaSubmissao.estaEmDrenagem()).isFalse();
        assertThat(filaSubmissao.estaVazia()).isTrue();

        SubmissaoModel rejeitada = aguardarStatus(
            submissaoRejeitadaId,
            StatusSubmissao.FALHA
        );

        assertThat(rejeitada.getStatus()).isEqualTo(StatusSubmissao.FALHA);
        assertThat(historicoDoBanco(submissaoRejeitadaId))
            .extracting(HistoricoStatusSubmissaoModel::getStatus)
            .containsExactly(
                StatusSubmissao.SUBMETIDA,
                StatusSubmissao.EM_FILA,
                StatusSubmissao.FALHA
            );
        assertThat(historicoDoBanco(submissaoRejeitadaId).get(2).getObservacao())
            .contains("fila de processamento");

        portao.countDown();
        avaliadorControlavel.liberarAvaliacoes();
        filaDoExecutor.clear();
        await().atMost(TEMPO_MAXIMO).untilAsserted(this::assertFilaOciosa);
    }

    @Test
    void deveIncrementarVersaoDasSubmissoesDaMesmaAtividade() throws Exception {
        Long primeiraId = submeterERetornarId(
            estudante.getId(),
            ATIVIDADE_ID,
            "public class Primeira { }"
        );
        aguardarStatus(primeiraId, StatusSubmissao.CONCLUIDA);

        mockMvc.perform(post("/submissoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSubmissao(estudante.getId(), ATIVIDADE_ID, "public class Segunda { }")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.versao").value(2));
    }

    @Test
    void deveCalcularVersoesSequenciaisSemDuplicidadeParaSubmissoesSimultaneas() throws Exception {
        for (int indice = 0; indice < 5; indice++) {
            submeterERetornarId(estudante.getId(), ATIVIDADE_ID, "public class Versao" + indice + " { }");
        }

        await().atMost(TEMPO_MAXIMO).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            List<Integer> versoes = submissaoRepository
                .findByEstudanteIdAndAtividadeIdOrderByVersaoDesc(
                    estudante.getId(),
                    ATIVIDADE_ID
                )
                .stream()
                .map(SubmissaoModel::getVersao)
                .toList();

            assertThat(versoes).containsExactly(5, 4, 3, 2, 1);
        });
    }

    @Test
    void deveImpedirVersaoDuplicadaPelaConstraintDeUnicidade() {
        submissaoRepository.saveAndFlush(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO, 1)
        );

        assertThatThrownBy(() -> submissaoRepository.saveAndFlush(
                new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO, 1)
            ))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deveImpedirConsultaDeSubmissaoPorOutroEstudante() throws Exception {
        Long submissaoId = submeterERetornarId(estudante.getId(), ATIVIDADE_ID, CODIGO);
        aguardarStatus(submissaoId, StatusSubmissao.CONCLUIDA);

        EstudanteModel outro = estudanteRepository.save(
            new EstudanteModel("Bruno Souza", "2026002", "bruno@example.com")
        );

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}",
                outro.getId(),
                submissaoId
            ))
            .andExpect(status().isNotFound());

        mockMvc.perform(get(
                "/submissoes/estudante/{estudanteId}/{submissaoId}/historico",
                outro.getId(),
                submissaoId
            ))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/submissoes/estudante/{estudanteId}", outro.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveImpedirQueOutroEstudanteEncaminheSubmissaoParaProcessamento() throws Exception {
        Long submissaoId = criarSubmissaoSemEncaminhar();

        EstudanteModel outro = estudanteRepository.save(
            new EstudanteModel("Bruno Souza", "2026002", "bruno@example.com")
        );

        mockMvc.perform(post("/submissoes/{submissaoId}/encaminhar", submissaoId)
                .param("estudanteId", String.valueOf(outro.getId())))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/submissoes/{submissaoId}/encaminhar", submissaoId))
            .andExpect(status().isBadRequest());

        SubmissaoModel submissao = submissaoRepository.findById(submissaoId).orElseThrow();

        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.SUBMETIDA);
        assertThat(filaSubmissao.estaVazia()).isTrue();
    }

    @Test
    void devePermitirQueODonoEncaminheSuaSubmissao() throws Exception {
        Long submissaoId = criarSubmissaoSemEncaminhar();

        mockMvc.perform(post("/submissoes/{submissaoId}/encaminhar", submissaoId)
                .param("estudanteId", String.valueOf(estudante.getId())))
            .andExpect(status().isAccepted());

        aguardarStatus(submissaoId, StatusSubmissao.CONCLUIDA);
    }

    @Test
    void deveRejeitarEncaminhamentoDeSubmissaoJaConcluida() throws Exception {
        Long submissaoId = submeterERetornarId(estudante.getId(), ATIVIDADE_ID, CODIGO);
        aguardarStatus(submissaoId, StatusSubmissao.CONCLUIDA);

        mockMvc.perform(post("/submissoes/{submissaoId}/encaminhar", submissaoId)
                .param("estudanteId", String.valueOf(estudante.getId())))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRejeitarSubmissaoComCodigoFonteVazio() throws Exception {
        mockMvc.perform(post("/submissoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSubmissao(estudante.getId(), ATIVIDADE_ID, "   ")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarNotFoundParaEstudanteInexistente() throws Exception {
        mockMvc.perform(get("/submissoes/estudante/{estudanteId}", 999999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarTransicoesInvalidasPeloPadraoState() {
        SubmissaoModel submissao = submissaoRepository.save(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO, 1)
        );

        assertThatThrownBy(submissao::iniciarProcessamento)
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Submetida");

        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();

        assertThatThrownBy(() -> submissao.falhar("Erro"))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Concluída");

        assertThatThrownBy(submissao::encaminharParaFila)
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Concluída");
    }

    private Long submeterERetornarId(Long estudanteId, Long atividadeId, String codigoFonte)
        throws Exception {
        MvcResult resultado = mockMvc.perform(post("/submissoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonSubmissao(estudanteId, atividadeId, codigoFonte)))
            .andExpect(status().isCreated())
            .andReturn();

        String location = resultado.getResponse().getHeader("Location");

        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private Long criarSubmissaoSemEncaminhar() {
        int proximaVersao = submissaoRepository
            .findTopByEstudanteIdAndAtividadeIdOrderByVersaoDesc(estudante.getId(), ATIVIDADE_ID)
            .map(SubmissaoModel::getVersao)
            .orElse(0) + 1;

        return submissaoRepository.save(
            new SubmissaoModel(estudante, ATIVIDADE_ID, CODIGO, proximaVersao)
        ).getId();
    }

    private String jsonSubmissao(Long estudanteId, Long atividadeId, String codigoFonte) {
        return """
            {
                "estudanteId": %d,
                "atividadeId": %d,
                "codigoFonte": "%s"
            }
            """.formatted(estudanteId, atividadeId, codigoFonte);
    }

    private SubmissaoModel aguardarStatus(Long submissaoId, StatusSubmissao esperado) {
        await().atMost(TEMPO_MAXIMO).pollInterval(Duration.ofMillis(50)).untilAsserted(() -> {
            SubmissaoModel submissao = submissaoRepository.findById(submissaoId).orElseThrow();

            assertThat(submissao.getStatus()).isEqualTo(esperado);
        });

        return submissaoRepository.findById(submissaoId).orElseThrow();
    }

    private List<HistoricoStatusSubmissaoModel> historicoDoBanco(Long submissaoId) {
        return historicoRepository.findBySubmissaoIdOrderByDataHoraAscIdAsc(submissaoId);
    }

    private void aguardarFilaOciosa() {
        await().atMost(TEMPO_MAXIMO).pollInterval(Duration.ofMillis(50)).untilAsserted(
            this::assertFilaOciosa
        );
    }

    private void assertFilaOciosa() {
        assertThat(filaSubmissao.estaVazia()).isTrue();
        assertThat(filaSubmissao.tentativasDeAgendamentoEmAndamento()).isZero();
        assertThat(filaSubmissao.estaEmDrenagem()).isFalse();
    }

    private static final class TarefaBloqueante implements Runnable {

        private final CountDownLatch portao;

        private TarefaBloqueante(CountDownLatch portao) {
            this.portao = portao;
        }

        @Override
        public void run() {
            try {
                portao.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
