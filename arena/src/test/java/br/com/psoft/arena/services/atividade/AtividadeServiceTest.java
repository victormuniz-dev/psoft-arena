package br.com.psoft.arena.services.atividade;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;
import br.com.psoft.arena.services.problema.ProblemaService;
import br.com.psoft.arena.services.turma.TurmaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtividadeService")
class AtividadeServiceTest {

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private TurmaService turmaService;

    @Mock
    private ProblemaService problemaService;

    @Mock
    private EstrategiaAvaliacaoService estrategiaService;

    @Mock
    private TurmaModel turma;

    @Mock
    private EstrategiaAvaliacaoModel estrategia;

    @Mock
    private EstrategiaAvaliacaoModel outraEstrategia;

    @InjectMocks
    private AtividadeService service;

    private AtividadeModel atividade;

    @BeforeEach
    void setUp() {
        atividade = new AtividadeModel("Lista");
    }

    private ProblemaModel problemaAtivo(String titulo) {
        return new ProblemaModel(titulo, "Descrição", List.of("RF1"), List.of("RP1"));
    }

    private ProblemaModel problemaInativo(String titulo) {
        ProblemaModel problema = problemaAtivo(titulo);
        problema.desativar();
        return problema;
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("cria a atividade com problemas e estratégias e a persiste")
        void criaAtividadeCompleta() {
            ProblemaModel p1 = problemaAtivo("P1");
            ProblemaModel p2 = problemaAtivo("P2");
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L, 20L))).thenReturn(List.of(p1, p2));
            when(estrategiaService.buscarEntidadesPorIds(List.of(5L))).thenReturn(List.of(estrategia));
            when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> inv.getArgument(0));

            AtividadeModel resultado = service.criar(1L, "Lista 1", List.of(10L, 20L), List.of(5L));

            assertEquals("Lista 1", resultado.getTitulo());
            assertEquals(2, resultado.getProblemas().size());
            assertTrue(resultado.getProblemas().contains(p1));
            assertTrue(resultado.getProblemas().contains(p2));
            assertEquals(1, resultado.getEstrategias().size());
            assertTrue(resultado.getEstrategias().contains(estrategia));
            verify(turma).adicionarAtividade(resultado);
            verify(atividadeRepository).save(resultado);
        }

        @Test
        @DisplayName("remove espaços do início e do fim do título")
        void aparaTitulo() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaAtivo("P1")));
            when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> inv.getArgument(0));

            AtividadeModel resultado = service.criar(1L, "  Lista 1  ", List.of(10L), null);

            assertEquals("Lista 1", resultado.getTitulo());
        }

        @Test
        @DisplayName("não consulta o serviço de estratégias quando a lista é nula")
        void estrategiasNulas() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaAtivo("P1")));
            when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> inv.getArgument(0));

            AtividadeModel resultado = service.criar(1L, "Lista", List.of(10L), null);

            assertTrue(resultado.getEstrategias().isEmpty());
            verifyNoInteractions(estrategiaService);
        }

        @Test
        @DisplayName("não consulta o serviço de estratégias quando a lista é vazia")
        void estrategiasVazias() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaAtivo("P1")));
            when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> inv.getArgument(0));

            AtividadeModel resultado = service.criar(1L, "Lista", List.of(10L), List.of());

            assertTrue(resultado.getEstrategias().isEmpty());
            verifyNoInteractions(estrategiaService);
        }

        @Test
        @DisplayName("remove ids repetidos antes de buscar problemas e estratégias")
        void removeIdsRepetidos() {
            ProblemaModel p1 = problemaAtivo("P1");
            ProblemaModel p2 = problemaAtivo("P2");
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L, 20L))).thenReturn(List.of(p1, p2));
            when(estrategiaService.buscarEntidadesPorIds(List.of(5L))).thenReturn(List.of(estrategia));
            when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> inv.getArgument(0));

            AtividadeModel resultado = service.criar(1L, "Lista", List.of(10L, 10L, 20L), List.of(5L, 5L));

            assertEquals(2, resultado.getProblemas().size());
            assertEquals(1, resultado.getEstrategias().size());
            verify(problemaService).buscarPorIds(List.of(10L, 20L));
            verify(estrategiaService).buscarEntidadesPorIds(List.of(5L));
        }

        @Test
        @DisplayName("lança exceção quando a lista de problemas é nula")
        void problemasNulos() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", null, List.of(5L)));

            assertEquals("A atividade precisa ter pelo menos um problema", ex.getMessage());
            verifyNoInteractions(turmaService, problemaService, estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("lança exceção quando a lista de problemas é vazia")
        void problemasVazios() {
            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", List.of(), List.of(5L)));

            assertEquals("A atividade precisa ter pelo menos um problema", ex.getMessage());
            verifyNoInteractions(turmaService, problemaService, estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("propaga a exceção quando a turma não existe")
        void turmaNaoEncontrada() {
            when(turmaService.buscarEntidadePorId(99L))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.criar(99L, "Lista", List.of(10L), null));

            assertEquals("Turma não encontrada: 99", ex.getMessage());
            verifyNoInteractions(problemaService, estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("lança exceção e não persiste quando algum problema está inativo")
        void rejeitaProblemaInativo() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaInativo("P1")));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", List.of(10L), null));

            assertEquals("Problemas inativos não podem ser associados a atividades", ex.getMessage());
            verify(turma, never()).adicionarAtividade(any(AtividadeModel.class));
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando o serviço devolve o mesmo problema duas vezes")
        void problemaDuplicado() {
            ProblemaModel p1 = problemaAtivo("P1");
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(p1, p1));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", List.of(10L), null));

            assertEquals("O problema já está nesta atividade", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando o serviço devolve a mesma estratégia duas vezes")
        void estrategiaDuplicada() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaAtivo("P1")));
            when(estrategiaService.buscarEntidadesPorIds(List.of(5L)))
                    .thenReturn(List.of(estrategia, estrategia));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", List.of(10L), List.of(5L)));

            assertEquals("A estratégia já está nesta atividade", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção e não persiste quando a turma está encerrada")
        void turmaEncerrada() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorIds(List.of(10L))).thenReturn(List.of(problemaAtivo("P1")));
            doThrow(new BusinessRuleException("Uma turma encerrada não pode receber novas atividades"))
                    .when(turma).adicionarAtividade(any(AtividadeModel.class));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", List.of(10L), null));

            assertEquals("Uma turma encerrada não pode receber novas atividades", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("retorna a atividade encontrada")
        void retornaAtividade() {
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));

            AtividadeModel resultado = service.buscarPorId(1L);

            assertSame(atividade, resultado);
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.buscarPorId(99L));

            assertEquals("Atividade não encontrada: 99", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("listarDaTurma")
    class ListarDaTurma {

        @Test
        @DisplayName("valida a turma e retorna as atividades dela")
        void retornaAtividadesDaTurma() {
            AtividadeModel outra = new AtividadeModel("Lista 2");
            when(atividadeRepository.findByTurmaId(1L)).thenReturn(List.of(atividade, outra));

            List<AtividadeModel> resultado = service.listarDaTurma(1L);

            assertEquals(List.of(atividade, outra), resultado);
            InOrder ordem = inOrder(turmaService, atividadeRepository);
            ordem.verify(turmaService).buscarPorId(1L);
            ordem.verify(atividadeRepository).findByTurmaId(1L);
        }

        @Test
        @DisplayName("retorna lista vazia quando a turma não tem atividades")
        void turmaSemAtividades() {
            when(atividadeRepository.findByTurmaId(1L)).thenReturn(List.of());

            List<AtividadeModel> resultado = service.listarDaTurma(1L);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("propaga a exceção e não consulta atividades quando a turma não existe")
        void turmaNaoEncontrada() {
            when(turmaService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            assertThrows(ResourceNotFoundException.class, () -> service.listarDaTurma(99L));

            verifyNoInteractions(atividadeRepository);
        }
    }

    @Nested
    @DisplayName("adicionarProblema")
    class AdicionarProblema {

        @Test
        @DisplayName("adiciona o problema à atividade")
        void adicionaProblema() {
            ProblemaModel problema = problemaAtivo("P1");
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(problema);

            AtividadeModel resultado = service.adicionarProblema(1L, 10L);

            assertSame(atividade, resultado);
            assertTrue(resultado.getProblemas().contains(problema));
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.adicionarProblema(99L, 10L));

            verifyNoInteractions(problemaService);
        }

        @Test
        @DisplayName("propaga a exceção quando o problema não existe")
        void problemaNaoEncontrado() {
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Problema não encontrado: 99"));

            assertThrows(ResourceNotFoundException.class, () -> service.adicionarProblema(1L, 99L));

            assertTrue(atividade.getProblemas().isEmpty());
        }

        @Test
        @DisplayName("lança exceção quando o problema está inativo")
        void rejeitaProblemaInativo() {
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(problemaInativo("P1"));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.adicionarProblema(1L, 10L));

            assertEquals("Problemas inativos não podem ser associados a atividades", ex.getMessage());
            assertTrue(atividade.getProblemas().isEmpty());
        }

        @Test
        @DisplayName("lança exceção quando o problema já está na atividade")
        void problemaJaAssociado() {
            ProblemaModel problema = problemaAtivo("P1");
            atividade.adicionarProblema(problema);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(problema);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.adicionarProblema(1L, 10L));

            assertEquals("O problema já está nesta atividade", ex.getMessage());
            assertEquals(1, atividade.getProblemas().size());
        }
    }

    @Nested
    @DisplayName("removerProblema")
    class RemoverProblema {

        @Test
        @DisplayName("remove o problema quando a atividade mantém ao menos um")
        void removeProblema() {
            ProblemaModel p1 = problemaAtivo("P1");
            ProblemaModel p2 = problemaAtivo("P2");
            atividade.adicionarProblema(p1);
            atividade.adicionarProblema(p2);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(p1);

            AtividadeModel resultado = service.removerProblema(1L, 10L);

            assertSame(atividade, resultado);
            assertEquals(1, resultado.getProblemas().size());
            assertTrue(resultado.getProblemas().contains(p2));
            assertFalse(resultado.getProblemas().contains(p1));
        }

        @Test
        @DisplayName("lança exceção ao remover o último problema da atividade")
        void removeUltimoProblema() {
            ProblemaModel p1 = problemaAtivo("P1");
            atividade.adicionarProblema(p1);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(p1);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.removerProblema(1L, 10L));

            assertEquals("A atividade precisa ter pelo menos um problema", ex.getMessage());
        }

        @Test
        @DisplayName("lança exceção quando o problema não pertence à atividade")
        void problemaNaoPertence() {
            ProblemaModel p1 = problemaAtivo("P1");
            ProblemaModel p2 = problemaAtivo("P2");
            atividade.adicionarProblema(p2);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(problemaService.buscarPorId(10L)).thenReturn(p1);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.removerProblema(1L, 10L));

            assertEquals("O problema não pertence a esta atividade", ex.getMessage());
            assertEquals(1, atividade.getProblemas().size());
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.removerProblema(99L, 10L));

            verifyNoInteractions(problemaService);
        }
    }

    @Nested
    @DisplayName("adicionarEstrategia")
    class AdicionarEstrategia {

        @Test
        @DisplayName("adiciona a estratégia à atividade")
        void adicionaEstrategia() {
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            AtividadeModel resultado = service.adicionarEstrategia(1L, 5L);

            assertSame(atividade, resultado);
            assertTrue(resultado.getEstrategias().contains(estrategia));
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.adicionarEstrategia(99L, 5L));

            verifyNoInteractions(estrategiaService);
        }

        @Test
        @DisplayName("propaga a exceção quando a estratégia não existe")
        void estrategiaNaoEncontrada() {
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(99L))
                    .thenThrow(new ResourceNotFoundException("Estratégia não encontrada: 99"));

            assertThrows(ResourceNotFoundException.class, () -> service.adicionarEstrategia(1L, 99L));

            assertTrue(atividade.getEstrategias().isEmpty());
        }

        @Test
        @DisplayName("lança exceção quando a estratégia já está na atividade")
        void estrategiaJaAssociada() {
            atividade.adicionarEstrategia(estrategia);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.adicionarEstrategia(1L, 5L));

            assertEquals("A estratégia já está nesta atividade", ex.getMessage());
            assertEquals(1, atividade.getEstrategias().size());
        }
    }

    @Nested
    @DisplayName("removerEstrategia")
    class RemoverEstrategia {

        @Test
        @DisplayName("remove a estratégia da atividade")
        void removeEstrategia() {
            atividade.adicionarEstrategia(estrategia);
            atividade.adicionarEstrategia(outraEstrategia);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            AtividadeModel resultado = service.removerEstrategia(1L, 5L);

            assertSame(atividade, resultado);
            assertEquals(1, resultado.getEstrategias().size());
            assertTrue(resultado.getEstrategias().contains(outraEstrategia));
        }

        @Test
        @DisplayName("permite remover a última estratégia da atividade")
        void removeUltimaEstrategia() {
            atividade.adicionarEstrategia(estrategia);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            AtividadeModel resultado = service.removerEstrategia(1L, 5L);

            assertTrue(resultado.getEstrategias().isEmpty());
        }

        @Test
        @DisplayName("lança exceção quando a estratégia não pertence à atividade")
        void estrategiaNaoPertence() {
            atividade.adicionarEstrategia(outraEstrategia);
            when(atividadeRepository.findById(1L)).thenReturn(Optional.of(atividade));
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.removerEstrategia(1L, 5L));

            assertEquals("A estratégia não pertence a esta atividade", ex.getMessage());
            assertEquals(1, atividade.getEstrategias().size());
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.removerEstrategia(99L, 5L));

            verifyNoInteractions(estrategiaService);
        }
    }
}