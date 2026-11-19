package br.com.psoft.arena.services.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.exception.AvaliacaoFalhaException;
import br.com.psoft.arena.exception.LLMIndisponivelException;
import br.com.psoft.arena.exception.RespostaLLMInvalidaException;
import br.com.psoft.arena.models.atividade.AtividadeModel;          
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;        
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;         
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;         
import br.com.psoft.arena.client.LLMClient;                  
import br.com.psoft.arena.client.RespostaLLM;                 
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    private static final Long SUBMISSAO_ID = 1L;
    private static final String PROMPT = "prompt montado";

    @Mock private SubmissaoRepository submissaoRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private PromptBuilder promptBuilder;
    @Mock private LLMClient llmClient;

    private AvaliacaoService service;

    @BeforeEach
    void setUp() {
        service = new AvaliacaoService(
                submissaoRepository,
                avaliacaoRepository,
                promptBuilder,
                llmClient,
                new CalculadoraNotaFinal());

        // save devolve o próprio objeto salvo
        lenient().when(avaliacaoRepository.save(any(AvaliacaoModel.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- helpers ----------

    private ConfiguracaoLlmModel llm(String nome) {
        ConfiguracaoLlmModel llm = mock(ConfiguracaoLlmModel.class);
        lenient().when(llm.getNomeModelo()).thenReturn(nome);
        return llm;
    }

    private RespostaLLM resposta(double nota, String evidencias, String justificativa) {
        return new RespostaLLM(nota, evidencias, justificativa);
    }

    /** Prepara a submissão encontrada, com a atividade e seus LLMs. */
    private SubmissaoModel submissaoComLlms(List<ConfiguracaoLlmModel> llms) {
        SubmissaoModel submissao = mock(SubmissaoModel.class);
        AtividadeModel atividade = mock(AtividadeModel.class);

        when(submissaoRepository.findById(SUBMISSAO_ID)).thenReturn(Optional.of(submissao));
        when(submissao.getAtividade()).thenReturn(atividade);
        when(atividade.getConfiguracoesLlm()).thenReturn(llms);                 
        return submissao;
    }

    private void avaliacaoNaoExiste() {
        when(avaliacaoRepository.findBySubmissaoId(SUBMISSAO_ID)).thenReturn(Optional.empty());
    }

    // ---------- idempotência ----------

    @Test
    void deveDevolverAvaliacaoExistenteSemChamarLlm() {
        AvaliacaoModel existente = mock(AvaliacaoModel.class);
        when(avaliacaoRepository.findBySubmissaoId(SUBMISSAO_ID)).thenReturn(Optional.of(existente));

        AvaliacaoModel resultado = service.avaliar(SUBMISSAO_ID);

        assertSame(existente, resultado);
        verifyNoInteractions(llmClient, promptBuilder);
        verify(avaliacaoRepository, never()).save(any());
    }

    // ---------- pré-condições ----------

    @Test
    void deveLancarExcecaoQuandoSubmissaoNaoExiste() {
        avaliacaoNaoExiste();
        when(submissaoRepository.findById(SUBMISSAO_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.avaliar(SUBMISSAO_ID));

        verifyNoInteractions(llmClient);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoAtividadeNaoTemLlm() {
        avaliacaoNaoExiste();
        submissaoComLlms(List.of());

        assertThrows(AvaliacaoFalhaException.class, () -> service.avaliar(SUBMISSAO_ID));

        verifyNoInteractions(llmClient);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoListaDeLlmsENula() {
        avaliacaoNaoExiste();
        submissaoComLlms(null);

        assertThrows(AvaliacaoFalhaException.class, () -> service.avaliar(SUBMISSAO_ID));

        verify(avaliacaoRepository, never()).save(any());
    }

    // ---------- sucesso ----------

    @Test
    void deveAvaliarEPersistirComUmUnicoLlm() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(grok, PROMPT)).thenReturn(resposta(8.5, "boa estrutura", "código coeso"));

        service.avaliar(SUBMISSAO_ID);

        ArgumentCaptor<AvaliacaoModel> captor = ArgumentCaptor.forClass(AvaliacaoModel.class);
        verify(avaliacaoRepository).save(captor.capture());
        AvaliacaoModel salva = captor.getValue();

        assertEquals(1, salva.getResultados().size());
        assertEquals(0, new BigDecimal("8.5").compareTo(salva.getNotaFinal().getNota())); 
        assertEquals("[grok] boa estrutura", salva.getNotaFinal().getEvidencias());
        assertEquals("[grok] código coeso", salva.getNotaFinal().getJustificativa());
    }

    @Test
    void deveCalcularMediaERegistrarResultadoDeCadaLlm() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        ConfiguracaoLlmModel outro = llm("outro");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok, outro));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(grok, PROMPT)).thenReturn(resposta(8.0, "e1", "j1"));
        when(llmClient.avaliar(outro, PROMPT)).thenReturn(resposta(6.0, "e2", "j2"));

        service.avaliar(SUBMISSAO_ID);

        ArgumentCaptor<AvaliacaoModel> captor = ArgumentCaptor.forClass(AvaliacaoModel.class);
        verify(avaliacaoRepository).save(captor.capture());
        AvaliacaoModel salva = captor.getValue();

        assertEquals(2, salva.getResultados().size());
        assertEquals(0, new BigDecimal("7.0").compareTo(salva.getNotaFinal().getNota()));
        assertEquals("[grok] e1\n\n[outro] e2", salva.getNotaFinal().getEvidencias());
    }

    @Test
    void deveEnviarOMesmoPromptParaTodosOsLlms() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        ConfiguracaoLlmModel outro = llm("outro");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok, outro));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(any(), any())).thenReturn(resposta(7.0, "e", "j"));

        service.avaliar(SUBMISSAO_ID);

        verify(promptBuilder, times(1)).montar(submissao);   // montado uma vez só
        verify(llmClient).avaliar(grok, PROMPT);
        verify(llmClient).avaliar(outro, PROMPT);
    }

    // ---------- falhas: nada pode ser salvo ----------

    @Test
    void deveFalharENaoSalvarQuandoUmLlmEstaIndisponivel() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        ConfiguracaoLlmModel outro = llm("outro");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok, outro));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(grok, PROMPT)).thenReturn(resposta(8.0, "e1", "j1"));
        when(llmClient.avaliar(outro, PROMPT)).thenThrow(new LLMIndisponivelException("fora do ar"));

        assertThrows(LLMIndisponivelException.class, () -> service.avaliar(SUBMISSAO_ID));

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveFalharENaoSalvarQuandoRespostaDeUmLlmEInvalida() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(grok, PROMPT)).thenThrow(new RespostaLLMInvalidaException("sem nota"));

        assertThrows(RespostaLLMInvalidaException.class, () -> service.avaliar(SUBMISSAO_ID));

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void excecoesDeLlmDevemSerAvaliacaoFalhaException() {
        avaliacaoNaoExiste();
        ConfiguracaoLlmModel grok = llm("grok");
        SubmissaoModel submissao = submissaoComLlms(List.of(grok));
        when(promptBuilder.montar(submissao)).thenReturn(PROMPT);
        when(llmClient.avaliar(grok, PROMPT)).thenThrow(new LLMIndisponivelException("timeout"));

        assertThrows(AvaliacaoFalhaException.class, () -> service.avaliar(SUBMISSAO_ID));
    }
}