package br.com.psoft.arena.controllers.atividade;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.psoft.arena.dtos.atividade.AtividadeResponseDto;
import br.com.psoft.arena.dtos.llm.ConfiguracaoLlmRequestDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.services.atividade.AtividadeService;

@WebMvcTest(AtividadeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AtividadeController")
class AtividadeControllerTest {

    private static final String DESCRICAO = "Descrição da lista";
    private static final LocalDateTime PRAZO = LocalDateTime.of(2030, 1, 1, 23, 59);
    private static final String CHAVE_API = "chave-secreta";
    private static final String CHAVE_CRIPTOGRAFADA = "Y2hhdmUtY3JpcHRvZ3JhZGE=";
    private static final String CORPO_VALIDO = """
            {"titulo":"Lista 1","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtividadeService atividadeService;

    private AtividadeModel criarAtividadeModel(Long id, String titulo, boolean concluida) {
        TurmaModel turma = new TurmaModel("Turma A", "T01", "2026.1", new ProfessorModel("Professor A"));
        ReflectionTestUtils.setField(turma, "id", 1L);

        ProblemaModel problema = new ProblemaModel("Problema 1", "Descrição", List.of("RF1"), List.of("RP1"));
        ReflectionTestUtils.setField(problema, "id", 10L);

        EstrategiaAvaliacaoModel estrategia = EstrategiaAvaliacaoModel.builder()
                .nome("Estratégia")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Descrição")
                .promptAvaliacao("Prompt")
                .build();
        ReflectionTestUtils.setField(estrategia, "id", 5L);

        AtividadeModel atividade = new AtividadeModel(titulo, DESCRICAO, PRAZO);
        ReflectionTestUtils.setField(atividade, "id", id);
        atividade.setTurma(turma);
        atividade.definirProblema(problema);
        atividade.definirEstrategia(estrategia);
        atividade.adicionarConfiguracaoLlm(
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", CHAVE_CRIPTOGRAFADA, true));
        if (concluida) {
            atividade.encerrar();
        }
        return atividade;
    }

    private AtividadeModel criarAtividadeModel(Long id, String titulo) {
        return criarAtividadeModel(id, titulo, false);
    }

    private AtividadeResponseDto criarResposta(Long id, String titulo) {
        return AtividadeResponseDto.de(criarAtividadeModel(id, titulo));
    }

    @Nested
    @DisplayName("POST /turmas/{turmaId}/atividades")
    class Criar {

        @Test
        @DisplayName("retorna 201 com Location e corpo da atividade criada")
        void retornaCreated() throws Exception {
            when(atividadeService.criar(
                    eq(1L), eq("Lista 1"), eq(DESCRICAO), eq(PRAZO), eq(10L), eq(5L),
                    any(ConfiguracaoLlmRequestDto.class)))
                    .thenReturn(criarResposta(7L, "Lista 1"));

            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/atividades/7"))
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.titulo").value("Lista 1"))
                    .andExpect(jsonPath("$.descricao").value(DESCRICAO))
                    .andExpect(jsonPath("$.prazoSubmissao").value("2030-01-01T23:59:00"))
                    .andExpect(jsonPath("$.status").value("ABERTA"))
                    .andExpect(jsonPath("$.problemaId").value(10))
                    .andExpect(jsonPath("$.estrategiaId").value(5))
                    .andExpect(jsonPath("$.configuracaoLlm.nomeModelo").value("GPT"))
                    .andExpect(jsonPath("$.configuracaoLlm.urlApi").value("https://api.exemplo.com"))
                    .andExpect(jsonPath("$.configuracaoLlm.padrao").value(true))
                    .andExpect(jsonPath("$.configuracaoLlm.chaveConfigurada").value(true));

            verify(atividadeService).criar(
                    eq(1L), eq("Lista 1"), eq(DESCRICAO), eq(PRAZO), eq(10L), eq(5L),
                    any(ConfiguracaoLlmRequestDto.class));
        }

        @Test
        @DisplayName("a resposta nunca contém a chave da API em texto puro ou criptografada")
        void naoExpoeChave() throws Exception {
            when(atividadeService.criar(
                    eq(1L), eq("Lista 1"), eq(DESCRICAO), eq(PRAZO), eq(10L), eq(5L),
                    any(ConfiguracaoLlmRequestDto.class)))
                    .thenReturn(criarResposta(7L, "Lista 1"));

            String corpo = mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.configuracaoLlm.chaveApi").doesNotExist())
                    .andExpect(jsonPath("$.configuracaoLlm.chaveApiCriptografada").doesNotExist())
                    .andReturn().getResponse().getContentAsString();

            assertFalse(corpo.contains(CHAVE_API));
            assertFalse(corpo.contains(CHAVE_CRIPTOGRAFADA));
        }

        @Test
        @DisplayName("retorna 400 quando o título está em branco")
        void tituloEmBranco() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando a descrição está em branco")
        void descricaoEmBranco() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando o prazo de submissão está ausente")
        void prazoAusente() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"Descrição da lista","problemaId":10,"estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando o problema está ausente")
        void problemaAusente() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando a estratégia está ausente")
        void estrategiaAusente() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":"chave-secreta"}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando a configuração de LLM está ausente")
        void configuracaoLlmAusente() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"estrategiaId":5}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando a chave da API está em branco")
        void chaveApiEmBranco() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","descricao":"Descrição da lista","prazoSubmissao":"2030-01-01T23:59:00","problemaId":10,"estrategiaId":5,"configuracaoLlm":{"nomeModelo":"GPT","urlApi":"https://api.exemplo.com","chaveApi":""}}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando o JSON é malformado")
        void jsonMalformado() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"titulo\":"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando o corpo está ausente")
        void semCorpo() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 400 quando o id da turma não é numérico")
        void turmaIdInvalido() throws Exception {
            mockMvc.perform(post("/turmas/abc/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }

        @Test
        @DisplayName("retorna 404 quando a turma não existe")
        void turmaNaoEncontrada() throws Exception {
            when(atividadeService.criar(
                    eq(99L), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            mockMvc.perform(post("/turmas/99/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 422 quando uma regra de negócio é violada")
        void regraDeNegocio() throws Exception {
            when(atividadeService.criar(
                    eq(1L), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new BusinessRuleException("Uma turma encerrada não pode receber novas atividades"));

            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /turmas/{turmaId}/atividades")
    class ListarDaTurma {

        @Test
        @DisplayName("retorna 200 com as atividades da turma")
        void retornaAtividades() throws Exception {
            when(atividadeService.listarDaTurma(1L))
                    .thenReturn(List.of(criarResposta(1L, "Lista 1"), criarResposta(2L, "Lista 2")));

            mockMvc.perform(get("/turmas/1/atividades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].titulo").value("Lista 1"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].titulo").value("Lista 2"));
        }

        @Test
        @DisplayName("retorna 200 com lista vazia quando a turma não tem atividades")
        void listaVazia() throws Exception {
            when(atividadeService.listarDaTurma(1L)).thenReturn(List.of());

            mockMvc.perform(get("/turmas/1/atividades"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("retorna 404 quando a turma não existe")
        void turmaNaoEncontrada() throws Exception {
            when(atividadeService.listarDaTurma(99L))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            mockMvc.perform(get("/turmas/99/atividades"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 400 quando o id da turma não é numérico")
        void turmaIdInvalido() throws Exception {
            mockMvc.perform(get("/turmas/abc/atividades"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }

    @Nested
    @DisplayName("GET /atividades/{id}")
    class Buscar {

        @Test
        @DisplayName("retorna 200 com a atividade encontrada")
        void retornaAtividade() throws Exception {
            when(atividadeService.buscarPorId(3L)).thenReturn(criarResposta(3L, "Lista 3"));

            mockMvc.perform(get("/atividades/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.titulo").value("Lista 3"))
                    .andExpect(jsonPath("$.problemaId").value(10))
                    .andExpect(jsonPath("$.estrategiaId").value(5))
                    .andExpect(jsonPath("$.configuracaoLlm.nomeModelo").value("GPT"));
        }

        @Test
        @DisplayName("retorna 404 quando a atividade não existe")
        void atividadeNaoEncontrada() throws Exception {
            when(atividadeService.buscarPorId(99L))
                    .thenThrow(new ResourceNotFoundException("Atividade não encontrada: 99"));

            mockMvc.perform(get("/atividades/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 400 quando o id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(get("/atividades/abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }

    @Nested
    @DisplayName("PATCH /atividades/{id}/encerrar")
    class Encerrar {

        @Test
        @DisplayName("retorna 200 com o DTO atualizado e status CONCLUIDA")
        void encerraAtividade() throws Exception {
            when(atividadeService.encerrar(7L))
                    .thenReturn(AtividadeResponseDto.de(criarAtividadeModel(7L, "Lista 1", true)));

            mockMvc.perform(patch("/atividades/7/encerrar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.status").value("CONCLUIDA"));

            verify(atividadeService).encerrar(7L);
        }

        @Test
        @DisplayName("retorna 404 quando a atividade não existe")
        void atividadeNaoEncontrada() throws Exception {
            when(atividadeService.encerrar(99L))
                    .thenThrow(new ResourceNotFoundException("Atividade não encontrada: 99"));

            mockMvc.perform(patch("/atividades/99/encerrar"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna 422 quando a atividade já está encerrada")
        void jaEncerrada() throws Exception {
            when(atividadeService.encerrar(7L))
                    .thenThrow(new BusinessRuleException("A atividade já está encerrada"));

            mockMvc.perform(patch("/atividades/7/encerrar"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("retorna 400 quando o id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(patch("/atividades/abc/encerrar"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }
}
