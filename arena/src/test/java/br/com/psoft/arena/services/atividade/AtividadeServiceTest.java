package br.com.psoft.arena.services.atividade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.dtos.atividade.AtividadeResponseDto;
import br.com.psoft.arena.dtos.llm.ConfiguracaoLlmRequestDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.services.criptografia.CriptografiaService;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;
import br.com.psoft.arena.services.problema.ProblemaService;
import br.com.psoft.arena.services.turma.TurmaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtividadeService")
class AtividadeServiceTest {

    private static final String DESCRICAO = "Descrição da lista";
    private static final String CHAVE_API = "chave-secreta";
    private static final String CHAVE_CRIPTOGRAFADA = "Y2hhdmUtY3JpcHRvZ3JhZGE=";
    private static final LocalDateTime PRAZO_FUTURO = LocalDateTime.now().plusDays(30);
    private static final LocalDateTime PRAZO_PASSADO = LocalDateTime.now().minusDays(1);

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private TurmaService turmaService;

    @Mock
    private ProblemaService problemaService;

    @Mock
    private EstrategiaAvaliacaoService estrategiaService;

    @Mock
    private CriptografiaService criptografiaService;

    @InjectMocks
    private AtividadeService service;

    private TurmaModel turma;
    private ProblemaModel problema;
    private EstrategiaAvaliacaoModel estrategia;
    private ConfiguracaoLlmRequestDto configuracaoLlm;

    @BeforeEach
    void setUp() {
        turma = new TurmaModel("Turma A", "T01", "2026.1", new ProfessorModel("Professor A"));
        ReflectionTestUtils.setField(turma, "id", 1L);

        problema = new ProblemaModel("P1", "Descrição", List.of("RF1"), List.of("RP1"));
        ReflectionTestUtils.setField(problema, "id", 10L);

        estrategia = EstrategiaAvaliacaoModel.builder()
                .nome("Estratégia")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Descrição")
                .promptAvaliacao("Prompt")
                .build();
        ReflectionTestUtils.setField(estrategia, "id", 5L);

        configuracaoLlm = new ConfiguracaoLlmRequestDto();
        configuracaoLlm.setNomeModelo("GPT");
        configuracaoLlm.setUrlApi("https://api.exemplo.com");
        configuracaoLlm.setChaveApi(CHAVE_API);
    }

    private AtividadeModel atividadeCompleta() {
        return atividadeCompleta(7L, "Lista");
    }

    private AtividadeModel atividadeCompleta(Long id, String titulo) {
        AtividadeModel atividade = new AtividadeModel(titulo, DESCRICAO, PRAZO_FUTURO);
        ReflectionTestUtils.setField(atividade, "id", id);
        atividade.setTurma(turma);
        atividade.definirProblema(problema);
        atividade.definirEstrategia(estrategia);
        atividade.adicionarConfiguracaoLlm(
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", CHAVE_CRIPTOGRAFADA, true));
        return atividade;
    }

    private void stubFluxoValido() {
        when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
        when(problemaService.buscarPorId(10L)).thenReturn(problema);
        when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
        when(criptografiaService.criptografar(CHAVE_API)).thenReturn(CHAVE_CRIPTOGRAFADA);
        when(atividadeRepository.save(any(AtividadeModel.class))).thenAnswer(inv -> {
            AtividadeModel salva = inv.getArgument(0);
            ReflectionTestUtils.setField(salva, "id", 7L);
            return salva;
        });
    }

    private AtividadeResponseDto criar() {
        return service.criar(1L, "Lista 1", DESCRICAO, PRAZO_FUTURO, 10L, 5L, configuracaoLlm);
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("cria a atividade com todos os dados válidos e retorna o DTO")
        void criaAtividadeCompleta() {
            stubFluxoValido();

            AtividadeResponseDto resultado = criar();

            assertEquals(7L, resultado.getId());
            assertEquals("Lista 1", resultado.getTitulo());
            assertEquals(DESCRICAO, resultado.getDescricao());
            assertEquals(PRAZO_FUTURO, resultado.getPrazoSubmissao());
            assertEquals(StatusAtividade.ABERTA, resultado.getStatus());
            assertEquals(1L, resultado.getTurmaId());
            assertEquals(10L, resultado.getProblemaId());
            assertEquals(5L, resultado.getEstrategiaId());
            assertEquals("GPT", resultado.getConfiguracaoLlm().getNomeModelo());
            assertEquals("https://api.exemplo.com", resultado.getConfiguracaoLlm().getUrlApi());
            assertTrue(resultado.getConfiguracaoLlm().isPadrao());
            assertTrue(resultado.getConfiguracaoLlm().isChaveConfigurada());
        }

        @Test
        @DisplayName("persiste exatamente um problema, uma estratégia e uma configuração LLM padrão")
        void persisteRelacionamentosUnicos() {
            stubFluxoValido();
            ArgumentCaptor<AtividadeModel> captor = ArgumentCaptor.forClass(AtividadeModel.class);

            criar();

            verify(atividadeRepository).save(captor.capture());
            AtividadeModel salva = captor.getValue();
            assertSame(problema, salva.getProblema());
            assertSame(estrategia, salva.getEstrategia());
            assertEquals(1, salva.getConfiguracoesLlm().size());
            assertTrue(salva.getConfiguracoesLlm().get(0).isPadrao());
            assertSame(salva, salva.getConfiguracoesLlm().get(0).getAtividade());
        }

        @Test
        @DisplayName("criptografa a chave da API antes de persistir")
        void criptografaChaveAntesDePersistir() {
            stubFluxoValido();
            ArgumentCaptor<AtividadeModel> captor = ArgumentCaptor.forClass(AtividadeModel.class);

            criar();

            verify(criptografiaService).criptografar(CHAVE_API);
            verify(atividadeRepository).save(captor.capture());
            String persistida = captor.getValue().getConfiguracoesLlm().get(0)
                    .getChaveApiCriptografada();
            assertEquals(CHAVE_CRIPTOGRAFADA, persistida);
            assertNotEquals(CHAVE_API, persistida);
        }

        @Test
        @DisplayName("remove espaços do título, da descrição e dos dados do LLM")
        void aparaCamposTextuais() {
            stubFluxoValido();
            configuracaoLlm.setNomeModelo("  GPT  ");
            configuracaoLlm.setUrlApi("  https://api.exemplo.com  ");
            ArgumentCaptor<AtividadeModel> captor = ArgumentCaptor.forClass(AtividadeModel.class);

            service.criar(1L, "  Lista 1  ", "  Descrição da lista  ",
                    PRAZO_FUTURO, 10L, 5L, configuracaoLlm);

            verify(atividadeRepository).save(captor.capture());
            AtividadeModel salva = captor.getValue();
            assertEquals("Lista 1", salva.getTitulo());
            assertEquals(DESCRICAO, salva.getDescricao());
            assertEquals("GPT", salva.getConfiguracoesLlm().get(0).getNomeModelo());
            assertEquals("https://api.exemplo.com", salva.getConfiguracoesLlm().get(0).getUrlApi());
        }

        @Test
        @DisplayName("lança exceção e não persiste quando a turma está encerrada")
        void turmaEncerrada() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            turma.encerrar();

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, AtividadeServiceTest.this::criar);

            assertEquals("Uma turma encerrada não pode receber novas atividades", ex.getMessage());
            verifyNoInteractions(problemaService, estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("propaga a exceção quando a turma não existe")
        void turmaNaoEncontrada() {
            when(turmaService.buscarEntidadePorId(99L))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.criar(99L, "Lista", DESCRICAO, PRAZO_FUTURO, 10L, 5L, configuracaoLlm));

            assertEquals("Turma não encontrada: 99", ex.getMessage());
            verifyNoInteractions(problemaService, estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("lança exceção e não persiste quando o problema está inativo")
        void problemaInativo() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            problema.desativar();
            when(problemaService.buscarPorId(10L)).thenReturn(problema);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, AtividadeServiceTest.this::criar);

            assertEquals("Problemas inativos não podem ser associados a atividades", ex.getMessage());
            verifyNoInteractions(estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("propaga a exceção quando o problema não existe")
        void problemaNaoEncontrado() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Problema não encontrado: 99"));

            assertThrows(ResourceNotFoundException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, PRAZO_FUTURO, 99L, 5L, configuracaoLlm));

            verifyNoInteractions(estrategiaService, atividadeRepository);
        }

        @Test
        @DisplayName("propaga a exceção quando a estratégia não existe")
        void estrategiaNaoEncontrada() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(99L))
                    .thenThrow(new ResourceNotFoundException("Estratégia de Avaliação não encontrada com id 99"));

            assertThrows(ResourceNotFoundException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, PRAZO_FUTURO, 10L, 99L, configuracaoLlm));

            verifyNoInteractions(atividadeRepository);
        }

        @Test
        @DisplayName("lança exceção e não persiste quando o prazo é passado")
        void prazoPassado() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, PRAZO_PASSADO, 10L, 5L, configuracaoLlm));

            assertEquals("O prazo de submissão deve ser futuro", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando o prazo é igual ou anterior ao momento da validação")
        void prazoIgualAoMomentoAtual() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
            LocalDateTime agora = LocalDateTime.now();

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, agora, 10L, 5L, configuracaoLlm));

            assertEquals("O prazo de submissão deve ser futuro", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção e não persiste quando o prazo é nulo")
        void prazoNulo() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, null, 10L, 5L, configuracaoLlm));

            assertEquals("O prazo de submissão é obrigatório", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando a descrição está em branco")
        void descricaoEmBranco() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", "   ", PRAZO_FUTURO, 10L, 5L, configuracaoLlm));

            assertEquals("A descrição é obrigatória", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando o título está em branco")
        void tituloEmBranco() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "   ", DESCRICAO, PRAZO_FUTURO, 10L, 5L, configuracaoLlm));

            assertEquals("O título é obrigatório", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando a configuração de LLM é nula")
        void configuracaoLlmNula() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.criar(1L, "Lista", DESCRICAO, PRAZO_FUTURO, 10L, 5L, null));

            assertEquals("A configuração de LLM é obrigatória", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando o nome do modelo está em branco")
        void nomeModeloEmBranco() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
            configuracaoLlm.setNomeModelo(" ");

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, AtividadeServiceTest.this::criar);

            assertEquals("O nome do modelo é obrigatório", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando a URL da API está em branco")
        void urlApiEmBranco() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
            configuracaoLlm.setUrlApi(" ");

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, AtividadeServiceTest.this::criar);

            assertEquals("A URL da API é obrigatória", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("lança exceção quando a chave da API está em branco")
        void chaveApiEmBranco() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
            configuracaoLlm.setChaveApi(" ");

            BusinessRuleException ex = assertThrows(BusinessRuleException.class, AtividadeServiceTest.this::criar);

            assertEquals("A chave da API é obrigatória", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }

        @Test
        @DisplayName("não persiste atividade quando a criptografia falha")
        void falhaNaCriptografiaNaoPersiste() {
            when(turmaService.buscarEntidadePorId(1L)).thenReturn(turma);
            when(problemaService.buscarPorId(10L)).thenReturn(problema);
            when(estrategiaService.buscarEntidadePorId(5L)).thenReturn(estrategia);
            when(criptografiaService.criptografar(CHAVE_API))
                    .thenThrow(new IllegalStateException("A chave mestra de criptografia não está configurada"));

            assertThrows(IllegalStateException.class, AtividadeServiceTest.this::criar);

            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("retorna o DTO da atividade encontrada")
        void retornaAtividade() {
            AtividadeModel atividade = atividadeCompleta();
            when(atividadeRepository.findById(7L)).thenReturn(Optional.of(atividade));

            AtividadeResponseDto resultado = service.buscarPorId(7L);

            assertEquals(7L, resultado.getId());
            assertEquals("Lista", resultado.getTitulo());
            assertEquals(10L, resultado.getProblemaId());
            assertEquals(5L, resultado.getEstrategiaId());
            assertTrue(resultado.getConfiguracaoLlm().isPadrao());
        }

        @Test
        @DisplayName("buscarEntidadePorId retorna a entidade encontrada")
        void entidadeRetornaAtividade() {
            AtividadeModel atividade = atividadeCompleta();
            when(atividadeRepository.findById(7L)).thenReturn(Optional.of(atividade));

            AtividadeModel resultado = service.buscarEntidadePorId(7L);

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
        @DisplayName("valida a turma e retorna os DTOs das atividades dela")
        void retornaAtividadesDaTurma() {
            AtividadeModel atividade = atividadeCompleta(1L, "Lista 1");
            AtividadeModel outra = atividadeCompleta(2L, "Lista 2");
            when(atividadeRepository.findByTurmaId(1L)).thenReturn(List.of(atividade, outra));

            List<AtividadeResponseDto> resultado = service.listarDaTurma(1L);

            assertEquals(2, resultado.size());
            assertEquals(1L, resultado.get(0).getId());
            assertEquals("Lista 1", resultado.get(0).getTitulo());
            assertEquals(2L, resultado.get(1).getId());
            assertEquals("Lista 2", resultado.get(1).getTitulo());
            InOrder ordem = inOrder(turmaService, atividadeRepository);
            ordem.verify(turmaService).buscarPorId(1L);
            ordem.verify(atividadeRepository).findByTurmaId(1L);
        }

        @Test
        @DisplayName("retorna lista vazia quando a turma não tem atividades")
        void turmaSemAtividades() {
            when(atividadeRepository.findByTurmaId(1L)).thenReturn(List.of());

            List<AtividadeResponseDto> resultado = service.listarDaTurma(1L);

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
    @DisplayName("encerrar")
    class Encerrar {

        @Test
        @DisplayName("encerra a atividade e retorna o DTO com status CONCLUIDA")
        void encerraAtividade() {
            AtividadeModel atividade = atividadeCompleta();
            when(atividadeRepository.findById(7L)).thenReturn(Optional.of(atividade));
            when(atividadeRepository.save(atividade)).thenReturn(atividade);

            AtividadeResponseDto resultado = service.encerrar(7L);

            assertEquals(StatusAtividade.CONCLUIDA, resultado.getStatus());
            assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
            verify(atividadeRepository).save(atividade);
        }

        @Test
        @DisplayName("lança exceção quando a atividade não existe")
        void atividadeNaoEncontrada() {
            when(atividadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.encerrar(99L));
        }

        @Test
        @DisplayName("não permite encerrar novamente uma atividade já concluída")
        void jaConcluida() {
            AtividadeModel atividade = atividadeCompleta();
            atividade.encerrar();
            when(atividadeRepository.findById(7L)).thenReturn(Optional.of(atividade));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> service.encerrar(7L));

            assertEquals("A atividade já está encerrada", ex.getMessage());
            verify(atividadeRepository, never()).save(any(AtividadeModel.class));
        }
    }
}
