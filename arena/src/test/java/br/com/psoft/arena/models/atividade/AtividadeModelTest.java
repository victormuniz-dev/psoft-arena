package br.com.psoft.arena.models.atividade;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtividadeModel")
class AtividadeModelTest {

    private static final String DESCRICAO = "Descrição da lista";
    private static final LocalDateTime PRAZO = LocalDateTime.of(2030, 1, 1, 23, 59);

    @Mock
    private ProblemaModel problema;

    @Mock
    private EstrategiaAvaliacaoModel estrategia;

    @Mock
    private TurmaModel turma;

    private AtividadeModel atividade;

    @BeforeEach
    void setUp() {
        atividade = new AtividadeModel("Lista 1", DESCRICAO, PRAZO);
    }

    @Nested
    @DisplayName("construção")
    class Construcao {

        @Test
        @DisplayName("construtor guarda os dados básicos e nasce sem relacionamentos")
        void construtorGuardaDadosBasicos() {
            assertEquals("Lista 1", atividade.getTitulo());
            assertEquals(DESCRICAO, atividade.getDescricao());
            assertEquals(PRAZO, atividade.getPrazoSubmissao());
            assertEquals(StatusAtividade.ABERTA, atividade.getStatus());
            assertNull(atividade.getId());
            assertNull(atividade.getTurma());
            assertNull(atividade.getProblema());
            assertNull(atividade.getEstrategia());
            assertTrue(atividade.getConfiguracoesLlm().isEmpty());
        }

        @Test
        @DisplayName("construtor sem argumentos mantém campos nulos e coleções vazias")
        void construtorSemArgumentos() {
            AtividadeModel vazia = new AtividadeModel();

            assertNull(vazia.getTitulo());
            assertNull(vazia.getDescricao());
            assertNull(vazia.getPrazoSubmissao());
            assertNull(vazia.getProblema());
            assertNull(vazia.getEstrategia());
            assertTrue(vazia.getConfiguracoesLlm().isEmpty());
        }

        @Test
        @DisplayName("setTurma associa a turma à atividade")
        void setTurma() {
            atividade.setTurma(turma);

            assertSame(turma, atividade.getTurma());
        }
    }

    @Nested
    @DisplayName("definirProblema")
    class DefinirProblema {

        @Test
        @DisplayName("associa um problema ativo")
        void associaProblemaAtivo() {
            when(problema.estaAtivo()).thenReturn(true);

            atividade.definirProblema(problema);

            assertSame(problema, atividade.getProblema());
            verify(problema).estaAtivo();
        }

        @Test
        @DisplayName("rejeita problema inativo e não o associa")
        void rejeitaProblemaInativo() {
            when(problema.estaAtivo()).thenReturn(false);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.definirProblema(problema));

            assertEquals("Problemas inativos não podem ser associados a atividades", ex.getMessage());
            assertNull(atividade.getProblema());
        }

        @Test
        @DisplayName("rejeita problema nulo")
        void rejeitaProblemaNulo() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.definirProblema(null));

            assertEquals("O problema é obrigatório", ex.getMessage());
            assertNull(atividade.getProblema());
        }
    }

    @Nested
    @DisplayName("definirEstrategia")
    class DefinirEstrategia {

        @Test
        @DisplayName("associa uma estratégia de avaliação")
        void associaEstrategia() {
            atividade.definirEstrategia(estrategia);

            assertSame(estrategia, atividade.getEstrategia());
        }

        @Test
        @DisplayName("rejeita estratégia nula, pois é obrigatória")
        void rejeitaEstrategiaNula() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.definirEstrategia(null));

            assertEquals("A estratégia de avaliação é obrigatória", ex.getMessage());
            assertNull(atividade.getEstrategia());
        }
    }

    @Nested
    @DisplayName("configurações de LLM")
    class ConfiguracoesLlm {

        @Test
        @DisplayName("adiciona configuração e associa o vínculo reverso com a atividade")
        void adicionaConfiguracao() {
            ConfiguracaoLlmModel configuracao =
                    new ConfiguracaoLlmModel("GPT", "https://api.exemplo", "criptografada", true);

            atividade.adicionarConfiguracaoLlm(configuracao);

            assertEquals(1, atividade.getConfiguracoesLlm().size());
            assertSame(configuracao, atividade.getConfiguracoesLlm().get(0));
            assertSame(atividade, configuracao.getAtividade());
        }

        @Test
        @DisplayName("retorna a configuração marcada como padrão")
        void retornaConfiguracaoPadrao() {
            ConfiguracaoLlmModel naoPadrao =
                    new ConfiguracaoLlmModel("Outro", "https://api.exemplo", "criptografada", false);
            ConfiguracaoLlmModel padrao =
                    new ConfiguracaoLlmModel("GPT", "https://api.exemplo", "criptografada", true);
            atividade.adicionarConfiguracaoLlm(naoPadrao);
            atividade.adicionarConfiguracaoLlm(padrao);

            assertSame(padrao, atividade.getConfiguracaoLlmPadrao());
        }

        @Test
        @DisplayName("retorna null quando não há configuração padrão")
        void semConfiguracaoPadrao() {
            assertNull(atividade.getConfiguracaoLlmPadrao());
        }

        @Test
        @DisplayName("getConfiguracoesLlm não permite alterar a coleção")
        void colecaoNaoModificavel() {
            ConfiguracaoLlmModel configuracao =
                    new ConfiguracaoLlmModel("GPT", "https://api.exemplo", "criptografada", true);
            atividade.adicionarConfiguracaoLlm(configuracao);

            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getConfiguracoesLlm().add(configuracao));
            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getConfiguracoesLlm().clear());
        }

        @Test
        @DisplayName("rejeita configuração nula")
        void rejeitaConfiguracaoNula() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.adicionarConfiguracaoLlm(null));

            assertEquals("A configuração de LLM é obrigatória", ex.getMessage());
            assertTrue(atividade.getConfiguracoesLlm().isEmpty());
        }
    }

    @Nested
    @DisplayName("ciclo de vida")
    class CicloDeVida {

        @Test
        @DisplayName("toda atividade nasce com status ABERTA")
        void nasceAberta() {
            assertEquals(StatusAtividade.ABERTA, atividade.getStatus());
        }

        @Test
        @DisplayName("encerrar altera o status para CONCLUIDA")
        void encerrarAlteraStatus() {
            atividade.encerrar();

            assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
        }

        @Test
        @DisplayName("não permite encerrar uma atividade já concluída")
        void naoPermiteEncerrarConcluida() {
            atividade.encerrar();

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.encerrar());

            assertEquals("A atividade já está encerrada", ex.getMessage());
            assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
        }
    }
}
