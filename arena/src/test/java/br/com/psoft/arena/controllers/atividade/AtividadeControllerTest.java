package br.com.psoft.arena.controllers.atividade;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.services.atividade.AtividadeService;

@WebMvcTest(AtividadeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AtividadeController")
class AtividadeControllerTest {

    private static final String CORPO_VALIDO = """
            {"titulo":"Lista 1","problemaIds":[10,20],"estrategiaIds":[5]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AtividadeService atividadeService;

    private AtividadeModel criarAtividade(Long id, String titulo) {
        TurmaModel turma = new TurmaModel("Turma A");
        ReflectionTestUtils.setField(turma, "id", 1L);

        ProblemaModel problema = new ProblemaModel("Problema 1", "Descrição", List.of("RF1"), List.of("RP1"));
        ReflectionTestUtils.setField(problema, "id", 10L);

        AtividadeModel atividade = new AtividadeModel(titulo);
        ReflectionTestUtils.setField(atividade, "id", id);
        atividade.setTurma(turma);
        atividade.adicionarProblema(problema);
        return atividade;
    }

    @Nested
    @DisplayName("POST /turmas/{turmaId}/atividades")
    class Criar {

        @Test
        @DisplayName("retorna 201 com Location e corpo da atividade criada")
        void retornaCreated() throws Exception {
            when(atividadeService.criar(1L, "Lista 1", List.of(10L, 20L), List.of(5L)))
                    .thenReturn(criarAtividade(7L, "Lista 1"));

            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/atividades/7"))
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.titulo").value("Lista 1"));

            verify(atividadeService).criar(1L, "Lista 1", List.of(10L, 20L), List.of(5L));
        }

        @Test
        @DisplayName("aceita requisição sem estratégias")
        void semEstrategias() throws Exception {
            when(atividadeService.criar(eq(1L), eq("Lista 1"), eq(List.of(10L)), any()))
                    .thenReturn(criarAtividade(7L, "Lista 1"));

            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"Lista 1","problemaIds":[10]}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/atividades/7"));
        }

        @Test
        @DisplayName("retorna 400 quando o título está em branco")
        void tituloEmBranco() throws Exception {
            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"titulo":"","problemaIds":[10]}
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
            when(atividadeService.criar(eq(99L), anyString(), anyList(), any()))
                    .thenThrow(new ResourceNotFoundException("Turma não encontrada: 99"));

            mockMvc.perform(post("/turmas/99/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna erro 4xx quando uma regra de negócio é violada")
        void regraDeNegocio() throws Exception {
            when(atividadeService.criar(eq(1L), anyString(), anyList(), any()))
                    .thenThrow(new BusinessRuleException("Uma turma encerrada não pode receber novas atividades"));

            mockMvc.perform(post("/turmas/1/atividades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CORPO_VALIDO))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /turmas/{turmaId}/atividades")
    class ListarDaTurma {

        @Test
        @DisplayName("retorna 200 com as atividades da turma")
        void retornaAtividades() throws Exception {
            when(atividadeService.listarDaTurma(1L))
                    .thenReturn(List.of(criarAtividade(1L, "Lista 1"), criarAtividade(2L, "Lista 2")));

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
            when(atividadeService.buscarPorId(3L)).thenReturn(criarAtividade(3L, "Lista 3"));

            mockMvc.perform(get("/atividades/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(3))
                    .andExpect(jsonPath("$.titulo").value("Lista 3"));
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
    @DisplayName("POST /atividades/{id}/problemas/{problemaId}")
    class AdicionarProblema {

        @Test
        @DisplayName("retorna 200 com a atividade atualizada")
        void adicionaProblema() throws Exception {
            when(atividadeService.adicionarProblema(1L, 10L)).thenReturn(criarAtividade(1L, "Lista 1"));

            mockMvc.perform(post("/atividades/1/problemas/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.titulo").value("Lista 1"));

            verify(atividadeService).adicionarProblema(1L, 10L);
        }

        @Test
        @DisplayName("retorna 404 quando a atividade ou o problema não existe")
        void naoEncontrado() throws Exception {
            when(atividadeService.adicionarProblema(1L, 99L))
                    .thenThrow(new ResourceNotFoundException("Problema não encontrado: 99"));

            mockMvc.perform(post("/atividades/1/problemas/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna erro 4xx quando o problema está inativo ou já está na atividade")
        void regraDeNegocio() throws Exception {
            when(atividadeService.adicionarProblema(1L, 10L))
                    .thenThrow(new BusinessRuleException("O problema já está nesta atividade"));

            mockMvc.perform(post("/atividades/1/problemas/10"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("retorna 400 quando algum id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(post("/atividades/1/problemas/abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }

    @Nested
    @DisplayName("DELETE /atividades/{id}/problemas/{problemaId}")
    class RemoverProblema {

        @Test
        @DisplayName("retorna 200 com a atividade atualizada")
        void removeProblema() throws Exception {
            when(atividadeService.removerProblema(1L, 10L)).thenReturn(criarAtividade(1L, "Lista 1"));

            mockMvc.perform(delete("/atividades/1/problemas/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(atividadeService).removerProblema(1L, 10L);
        }

        @Test
        @DisplayName("retorna 404 quando a atividade ou o problema não existe")
        void naoEncontrado() throws Exception {
            when(atividadeService.removerProblema(99L, 10L))
                    .thenThrow(new ResourceNotFoundException("Atividade não encontrada: 99"));

            mockMvc.perform(delete("/atividades/99/problemas/10"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna erro 4xx ao remover o último problema ou um problema que não pertence")
        void regraDeNegocio() throws Exception {
            when(atividadeService.removerProblema(1L, 10L))
                    .thenThrow(new BusinessRuleException("A atividade precisa ter pelo menos um problema"));

            mockMvc.perform(delete("/atividades/1/problemas/10"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("retorna 400 quando algum id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(delete("/atividades/abc/problemas/10"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }

    @Nested
    @DisplayName("POST /atividades/{id}/estrategias/{estrategiaId}")
    class AdicionarEstrategia {

        @Test
        @DisplayName("retorna 200 com a atividade atualizada")
        void adicionaEstrategia() throws Exception {
            when(atividadeService.adicionarEstrategia(1L, 5L)).thenReturn(criarAtividade(1L, "Lista 1"));

            mockMvc.perform(post("/atividades/1/estrategias/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.titulo").value("Lista 1"));

            verify(atividadeService).adicionarEstrategia(1L, 5L);
        }

        @Test
        @DisplayName("retorna 404 quando a atividade ou a estratégia não existe")
        void naoEncontrado() throws Exception {
            when(atividadeService.adicionarEstrategia(1L, 99L))
                    .thenThrow(new ResourceNotFoundException("Estratégia não encontrada: 99"));

            mockMvc.perform(post("/atividades/1/estrategias/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna erro 4xx quando a estratégia já está na atividade")
        void regraDeNegocio() throws Exception {
            when(atividadeService.adicionarEstrategia(1L, 5L))
                    .thenThrow(new BusinessRuleException("A estratégia já está nesta atividade"));

            mockMvc.perform(post("/atividades/1/estrategias/5"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("retorna 400 quando algum id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(post("/atividades/1/estrategias/abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }

    @Nested
    @DisplayName("DELETE /atividades/{id}/estrategias/{estrategiaId}")
    class RemoverEstrategia {

        @Test
        @DisplayName("retorna 200 com a atividade atualizada")
        void removeEstrategia() throws Exception {
            when(atividadeService.removerEstrategia(1L, 5L)).thenReturn(criarAtividade(1L, "Lista 1"));

            mockMvc.perform(delete("/atividades/1/estrategias/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));

            verify(atividadeService).removerEstrategia(1L, 5L);
        }

        @Test
        @DisplayName("retorna 404 quando a atividade ou a estratégia não existe")
        void naoEncontrado() throws Exception {
            when(atividadeService.removerEstrategia(99L, 5L))
                    .thenThrow(new ResourceNotFoundException("Atividade não encontrada: 99"));

            mockMvc.perform(delete("/atividades/99/estrategias/5"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("retorna erro 4xx quando a estratégia não pertence à atividade")
        void regraDeNegocio() throws Exception {
            when(atividadeService.removerEstrategia(1L, 5L))
                    .thenThrow(new BusinessRuleException("A estratégia não pertence a esta atividade"));

            mockMvc.perform(delete("/atividades/1/estrategias/5"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("retorna 400 quando algum id não é numérico")
        void idInvalido() throws Exception {
            mockMvc.perform(delete("/atividades/1/estrategias/abc"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(atividadeService);
        }
    }
}