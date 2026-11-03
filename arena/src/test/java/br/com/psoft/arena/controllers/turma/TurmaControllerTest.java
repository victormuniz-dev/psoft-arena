package br.com.psoft.arena.controllers.turma;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.services.turma.TurmaService;

@WebMvcTest(TurmaController.class)
class TurmaControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TurmaService turmaService;

    @Test
    @DisplayName("POST sem nome retorna 400")
    void criarSemNome() throws Exception {
        mockMvc.perform(post("/turmas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"codigo":"T01","periodoLetivo":"2026.2","professorId":1}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET de turma inexistente retorna 404")
    void turmaInexistente() throws Exception {
        when(turmaService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException("Turma", 99L));

        mockMvc.perform(get("/turmas/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Regra de negócio violada retorna 422")
    void regraViolada() throws Exception {
        when(turmaService.encerrar(1L))
            .thenThrow(new BusinessRuleException("A turma já está encerrada"));

        mockMvc.perform(patch("/turmas/1/encerrar"))
            .andExpect(status().isUnprocessableEntity());
    }
}
