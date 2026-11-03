package br.com.psoft.arena.models.atividade;

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
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtividadeModel")
class AtividadeModelTest {

    @Mock
    private ProblemaModel problema;

    @Mock
    private ProblemaModel outroProblema;

    @Mock
    private EstrategiaAvaliacaoModel estrategia;

    @Mock
    private EstrategiaAvaliacaoModel outraEstrategia;

    @Mock
    private TurmaModel turma;

    private AtividadeModel atividade;

    @BeforeEach
    void setUp() {
        atividade = new AtividadeModel("Lista 1");
    }

    @Nested
    @DisplayName("construção")
    class Construcao {

        @Test
        @DisplayName("construtor com título guarda o título e inicia coleções vazias")
        void construtorComTitulo() {
            assertEquals("Lista 1", atividade.getTitulo());
            assertNull(atividade.getId());
            assertNull(atividade.getTurma());
            assertTrue(atividade.getProblemas().isEmpty());
            assertTrue(atividade.getEstrategias().isEmpty());
        }

        @Test
        @DisplayName("construtor sem argumentos inicia coleções vazias e título nulo")
        void construtorSemArgumentos() {
            AtividadeModel vazia = new AtividadeModel();

            assertNull(vazia.getTitulo());
            assertTrue(vazia.getProblemas().isEmpty());
            assertTrue(vazia.getEstrategias().isEmpty());
        }

        @Test
        @DisplayName("setTurma associa a turma à atividade")
        void setTurma() {
            atividade.setTurma(turma);

            assertSame(turma, atividade.getTurma());
        }
    }

    @Nested
    @DisplayName("adicionarProblema")
    class AdicionarProblema {

        @Test
        @DisplayName("adiciona problema ativo")
        void adicionaProblemaAtivo() {
            when(problema.estaAtivo()).thenReturn(true);

            atividade.adicionarProblema(problema);

            assertEquals(1, atividade.getProblemas().size());
            assertTrue(atividade.getProblemas().contains(problema));
        }

        @Test
        @DisplayName("adiciona vários problemas distintos")
        void adicionaVariosProblemas() {
            when(problema.estaAtivo()).thenReturn(true);
            when(outroProblema.estaAtivo()).thenReturn(true);

            atividade.adicionarProblema(problema);
            atividade.adicionarProblema(outroProblema);

            assertEquals(2, atividade.getProblemas().size());
            assertTrue(atividade.getProblemas().contains(problema));
            assertTrue(atividade.getProblemas().contains(outroProblema));
        }

        @Test
        @DisplayName("rejeita problema inativo e não o adiciona")
        void rejeitaProblemaInativo() {
            when(problema.estaAtivo()).thenReturn(false);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.adicionarProblema(problema));

            assertEquals("Problemas inativos não podem ser associados a atividades", ex.getMessage());
            assertTrue(atividade.getProblemas().isEmpty());
            verify(problema).estaAtivo();
        }

        @Test
        @DisplayName("rejeita problema já associado e mantém apenas uma ocorrência")
        void rejeitaProblemaDuplicado() {
            when(problema.estaAtivo()).thenReturn(true);
            atividade.adicionarProblema(problema);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.adicionarProblema(problema));

            assertEquals("O problema já está nesta atividade", ex.getMessage());
            assertEquals(1, atividade.getProblemas().size());
        }
    }

    @Nested
    @DisplayName("removerProblema")
    class RemoverProblema {

        @Test
        @DisplayName("remove problema associado")
        void removeProblemaAssociado() {
            when(problema.estaAtivo()).thenReturn(true);
            atividade.adicionarProblema(problema);

            atividade.removerProblema(problema);

            assertTrue(atividade.getProblemas().isEmpty());
        }

        @Test
        @DisplayName("remove apenas o problema informado")
        void removeApenasOInformado() {
            when(problema.estaAtivo()).thenReturn(true);
            when(outroProblema.estaAtivo()).thenReturn(true);
            atividade.adicionarProblema(problema);
            atividade.adicionarProblema(outroProblema);

            atividade.removerProblema(problema);

            assertEquals(1, atividade.getProblemas().size());
            assertTrue(atividade.getProblemas().contains(outroProblema));
        }

        @Test
        @DisplayName("lança exceção ao remover problema que não pertence à atividade")
        void lancaExcecaoSeNaoPertence() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.removerProblema(problema));

            assertEquals("O problema não pertence a esta atividade", ex.getMessage());
        }

        @Test
        @DisplayName("não permite remover o mesmo problema duas vezes")
        void naoPermiteRemoverDuasVezes() {
            when(problema.estaAtivo()).thenReturn(true);
            atividade.adicionarProblema(problema);
            atividade.removerProblema(problema);

            assertThrows(BusinessRuleException.class,
                    () -> atividade.removerProblema(problema));
        }
    }

    @Nested
    @DisplayName("adicionarEstrategia")
    class AdicionarEstrategia {

        @Test
        @DisplayName("adiciona estratégia")
        void adicionaEstrategia() {
            atividade.adicionarEstrategia(estrategia);

            assertEquals(1, atividade.getEstrategias().size());
            assertTrue(atividade.getEstrategias().contains(estrategia));
        }

        @Test
        @DisplayName("adiciona várias estratégias distintas")
        void adicionaVariasEstrategias() {
            atividade.adicionarEstrategia(estrategia);
            atividade.adicionarEstrategia(outraEstrategia);

            assertEquals(2, atividade.getEstrategias().size());
        }

        @Test
        @DisplayName("rejeita estratégia já associada e mantém apenas uma ocorrência")
        void rejeitaEstrategiaDuplicada() {
            atividade.adicionarEstrategia(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.adicionarEstrategia(estrategia));

            assertEquals("A estratégia já está nesta atividade", ex.getMessage());
            assertEquals(1, atividade.getEstrategias().size());
        }
    }

    @Nested
    @DisplayName("removerEstrategia")
    class RemoverEstrategia {

        @Test
        @DisplayName("remove estratégia associada")
        void removeEstrategiaAssociada() {
            atividade.adicionarEstrategia(estrategia);

            atividade.removerEstrategia(estrategia);

            assertTrue(atividade.getEstrategias().isEmpty());
        }

        @Test
        @DisplayName("remove apenas a estratégia informada")
        void removeApenasAInformada() {
            atividade.adicionarEstrategia(estrategia);
            atividade.adicionarEstrategia(outraEstrategia);

            atividade.removerEstrategia(estrategia);

            assertEquals(1, atividade.getEstrategias().size());
            assertTrue(atividade.getEstrategias().contains(outraEstrategia));
        }

        @Test
        @DisplayName("lança exceção ao remover estratégia que não pertence à atividade")
        void lancaExcecaoSeNaoPertence() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> atividade.removerEstrategia(estrategia));

            assertEquals("A estratégia não pertence a esta atividade", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Imutabilidade das coleções expostas
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("coleções não modificáveis")
    class ColecoesNaoModificaveis {

        @Test
        @DisplayName("getProblemas não permite alterar o conjunto")
        void getProblemasImutavel() {
            when(problema.estaAtivo()).thenReturn(true);
            atividade.adicionarProblema(problema);

            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getProblemas().add(outroProblema));
            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getProblemas().remove(problema));
            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getProblemas().clear());
            assertEquals(1, atividade.getProblemas().size());
        }

        @Test
        @DisplayName("getEstrategias não permite alterar o conjunto")
        void getEstrategiasImutavel() {
            atividade.adicionarEstrategia(estrategia);

            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getEstrategias().add(outraEstrategia));
            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getEstrategias().remove(estrategia));
            assertThrows(UnsupportedOperationException.class,
                    () -> atividade.getEstrategias().clear());
            assertEquals(1, atividade.getEstrategias().size());
        }

        @Test
        @DisplayName("a visão retornada reflete alterações feitas pelos métodos da atividade")
        void visaoRefleteAlteracoes() {
            when(problema.estaAtivo()).thenReturn(true);
            var visaoProblemas = atividade.getProblemas();
            var visaoEstrategias = atividade.getEstrategias();

            atividade.adicionarProblema(problema);
            atividade.adicionarEstrategia(estrategia);

            assertTrue(visaoProblemas.contains(problema));
            assertTrue(visaoEstrategias.contains(estrategia));
        }
    }
}
