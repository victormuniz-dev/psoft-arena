package br.com.psoft.arena.controllers.turma;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import br.com.psoft.arena.models.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.EstudanteRepository;
import br.com.psoft.arena.repositories.professor.ProfessorRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TurmaEstudanteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private EstudanteRepository estudanteRepository;
    @Autowired private ProfessorRepository professorRepository;

    private ProfessorModel professor;

    @BeforeEach
    void limparBanco() {
        turmaRepository.deleteAll();   // apaga também as linhas da tabela de junção
        estudanteRepository.deleteAll();
        professorRepository.deleteAll();
        professor = professorRepository.save(new ProfessorModel("Prof. Teste"));
        estudanteRepository.save(new EstudanteModel("Ana", "123", "ana@ccc.ufcg.edu.br"));
    }

    private Long criarTurma(StatusTurma status) {
        TurmaModel turma = new TurmaModel();
        turma.setNome("PSoft");
        turma.setProfessorResponsavel(professor);
        ReflectionTestUtils.setField(turma, "codigo", "T01");
        ReflectionTestUtils.setField(turma, "periodoLetivo", "2026.2");
        ReflectionTestUtils.setField(turma, "status", status);
        return turmaRepository.save(turma).getId();
    }

    private void associar(Long turmaId, String matricula) throws Exception {
        mockMvc.perform(post("/turmas/{id}/estudantes", turmaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"" + matricula + "\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void deveAssociarEListarEstudante() throws Exception {
        Long id = criarTurma(StatusTurma.ATIVA);
        associar(id, "123");

        mockMvc.perform(get("/turmas/{id}/estudantes", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].matricula").value("123"));
    }

    @Test
    void naoDevePermitirAssociacaoDuplicada() throws Exception {
        Long id = criarTurma(StatusTurma.ATIVA);
        associar(id, "123");

        mockMvc.perform(post("/turmas/{id}/estudantes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"123\"}"))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoDeveAssociarEmTurmaEncerrada() throws Exception {
        Long id = criarTurma(StatusTurma.ENCERRADA);

        mockMvc.perform(post("/turmas/{id}/estudantes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"123\"}"))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRemoverEstudanteDaTurma() throws Exception {
        Long id = criarTurma(StatusTurma.ATIVA);
        associar(id, "123");

        mockMvc.perform(delete("/turmas/{id}/estudantes/123", id))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/turmas/{id}/estudantes", id))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void naoDeveRemoverEstudanteQueNaoPertenceATurma() throws Exception {
        Long id = criarTurma(StatusTurma.ATIVA);

        mockMvc.perform(delete("/turmas/{id}/estudantes/123", id))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar404ParaTurmaOuEstudanteInexistente() throws Exception {
        Long id = criarTurma(StatusTurma.ATIVA);

        mockMvc.perform(post("/turmas/{id}/estudantes", 9999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"123\"}"))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/turmas/{id}/estudantes", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"000\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void devePermitirMesmoEstudanteEmVariasTurmas() throws Exception {
        Long t1 = criarTurma(StatusTurma.ATIVA);
        Long t2 = criarTurma(StatusTurma.ATIVA);
        associar(t1, "123");
        associar(t2, "123");
    }
}