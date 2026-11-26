package br.com.psoft.arena.services.avaliacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.dtos.avaliacao.ResultadoAvaliacaoResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import br.com.psoft.arena.client.LLMClient;

@ExtendWith(MockitoExtension.class)
class AvaliacaoConsultaServiceTest {

    private static final Long ESTUDANTE_ID = 1L;
    private static final Long SUBMISSAO_ID = 10L;
    private static final Long ATIVIDADE_ID = 20L;

    @Mock private SubmissaoRepository submissaoRepository;
    @Mock private AtividadeRepository atividadeRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private PromptBuilder promptBuilder;
    @Mock private LLMClient llmClient;

    private AvaliacaoService service;

    @BeforeEach
    void configurar() {
        service = new AvaliacaoService(
            submissaoRepository,
            atividadeRepository,
            avaliacaoRepository,
            promptBuilder,
            llmClient,
            new CalculadoraNotaFinal()
        );
    }

    private SubmissaoModel criarSubmissao(StatusSubmissao status) {
        EstudanteModel estudante = new EstudanteModel(
            "Ana Silva",
            "2026001",
            "ana@example.com"
        );

        SubmissaoModel submissao = new SubmissaoModel(
            estudante,
            ATIVIDADE_ID,
            "public class Main {}",
            1
        );
        ReflectionTestUtils.setField(submissao, "id", SUBMISSAO_ID);

        switch (status) {
            case EM_FILA -> submissao.encaminharParaFila();
            case PROCESSANDO -> {
                submissao.encaminharParaFila();
                submissao.iniciarProcessamento();
            }
            case CONCLUIDA -> {
                submissao.encaminharParaFila();
                submissao.iniciarProcessamento();
                submissao.concluir();
            }
            case FALHA -> {
                submissao.encaminharParaFila();
                submissao.iniciarProcessamento();
                submissao.falhar("Falha simulada");
            }
            case SUBMETIDA -> {
                // Estado inicial.
            }
        }

        return submissao;
    }

    @Test
    void deveRetornarResultadoCompletoQuandoAvaliacaoEstiverConcluida() {
        SubmissaoModel submissao = criarSubmissao(StatusSubmissao.CONCLUIDA);
        AvaliacaoModel avaliacao = new AvaliacaoModel(
            submissao,
            new NotaFinal(
                new BigDecimal("8.5"),
                "Evidências de boa estrutura",
                "Código coeso e bem organizado"
            ),
            List.of()
        );

        when(submissaoRepository.findByIdAndEstudanteId(
            SUBMISSAO_ID, ESTUDANTE_ID
        )).thenReturn(Optional.of(submissao));

        when(avaliacaoRepository.findBySubmissaoId(SUBMISSAO_ID))
            .thenReturn(Optional.of(avaliacao));

        ResultadoAvaliacaoResponseDto resultado =
            service.consultarResultado(ESTUDANTE_ID, SUBMISSAO_ID);

        assertTrue(resultado.disponivel());
        assertEquals(SUBMISSAO_ID, resultado.submissaoId());
        assertEquals(StatusSubmissao.CONCLUIDA, resultado.situacao());
        assertEquals(0, new BigDecimal("8.5").compareTo(resultado.nota()));
        assertEquals("Código coeso e bem organizado", resultado.justificativa());
        assertEquals("Evidências de boa estrutura", resultado.evidencias());
        assertNotNull(resultado.dataAvaliacao());
        assertNotNull(resultado.resultados());
    }

    @ParameterizedTest
    @EnumSource(
        value = StatusSubmissao.class,
        names = {"SUBMETIDA", "EM_FILA", "PROCESSANDO", "FALHA"}
    )
    void naoDeveDisponibilizarResultadoAntesDaConclusao(StatusSubmissao status) {
        SubmissaoModel submissao = criarSubmissao(status);

        when(submissaoRepository.findByIdAndEstudanteId(
            SUBMISSAO_ID, ESTUDANTE_ID
        )).thenReturn(Optional.of(submissao));

        ResultadoAvaliacaoResponseDto resultado =
            service.consultarResultado(ESTUDANTE_ID, SUBMISSAO_ID);

        assertFalse(resultado.disponivel());
        assertEquals(status, resultado.situacao());
        assertNull(resultado.nota());
        assertNull(resultado.justificativa());
        assertNull(resultado.evidencias());
        assertNull(resultado.dataAvaliacao());
        assertTrue(resultado.resultados().isEmpty());

        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void deveImpedirConsultaQuandoSubmissaoNaoPertenceAoEstudante() {
        when(submissaoRepository.findByIdAndEstudanteId(
            SUBMISSAO_ID, ESTUDANTE_ID
        )).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> service.consultarResultado(ESTUDANTE_ID, SUBMISSAO_ID)
        );

        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void deveSinalizarInconsistenciaQuandoSubmissaoConcluidaNaoPossuiAvaliacao() {
        SubmissaoModel submissao = criarSubmissao(StatusSubmissao.CONCLUIDA);

        when(submissaoRepository.findByIdAndEstudanteId(
            SUBMISSAO_ID, ESTUDANTE_ID
        )).thenReturn(Optional.of(submissao));

        when(avaliacaoRepository.findBySubmissaoId(SUBMISSAO_ID))
            .thenReturn(Optional.empty());

        assertThrows(
            BusinessRuleException.class,
            () -> service.consultarResultado(ESTUDANTE_ID, SUBMISSAO_ID)
        );
    }
}
