package br.com.psoft.arena.controllers.estudante.consultaEstudante;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.psoft.arena.dtos.atividade.atividadeEstudante.AtividadeEstudanteResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaEstudante.TurmaEstudanteResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.services.estudante.consultaestudante.ConsultaAtividadeEstudanteService;

@WebMvcTest(ConsultaAtividadeEstudanteController.class)
class ConsultaAtividadeEstudanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaAtividadeEstudanteService consultaService;

    @Test
    @DisplayName("GET /api/estudante/{id}/turmas - Deve retornar HTTP 200 com turmas do estudante")
    void deveRetornar200AoListarTurmas() throws Exception {
        TurmaEstudanteResponseDto turmaDto = TurmaEstudanteResponseDto.builder()
                .id(1L)
                .nome("PSoft")
                .codigo("PSOFT-2026")
                .periodoLetivo("2026.1")
                .nomeProfessor("Prof. Dalton")
                .status(StatusTurma.ATIVA)
                .build();

        when(consultaService.listarTurmas(10L)).thenReturn(List.of(turmaDto));

        mockMvc.perform(get("/api/estudante/10/turmas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("PSoft"))
                .andExpect(jsonPath("$[0].codigo").value("PSOFT-2026"))
                .andExpect(jsonPath("$[0].nomeProfessor").value("Prof. Dalton"));
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/turma/{turmaId}/atividades - Deve retornar HTTP 200 com atividades da turma")
    void deveRetornar200AoListarAtividadesPorTurma() throws Exception {
       AtividadeEstudanteResponseDto atividadeDto = AtividadeEstudanteResponseDto.builder()
        .id(100L)
        .titulo("Atividade 01")
        .nomeTurma("PSoft")
        .codigo("PSOFT-2026")
        .descricao("Implementar US7")
        .prazo(LocalDateTime.now().plusDays(5))
        .status(StatusAtividade.ABERTA)
        .problemas(List.of())
        .estrategiaAvaliativa(List.of("Testes Unitários"))
        .build();

        when(consultaService.listarAtividadesPorTurma(10L, 1L)).thenReturn(List.of(atividadeDto));

        mockMvc.perform(get("/api/estudante/10/turma/1/atividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].titulo").value("Atividade 01"))
                .andExpect(jsonPath("$[0].codigo").value("PSOFT-2026"))
                .andExpect(jsonPath("$[0].configuracaoLlm").doesNotExist())
                .andExpect(jsonPath("$[0].status").value("ABERTA"));
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/turma/{turmaId}/atividades - Deve retornar HTTP 404 quando a turma não existe")
    void deveRetornar404QuandoTurmaNaoExiste() throws Exception {
        when(consultaService.listarAtividadesPorTurma(10L, 999L))
                .thenThrow(new ResourceNotFoundException("Turma não encontrada com o ID: 999"));

        mockMvc.perform(get("/api/estudante/10/turma/999/atividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/atividades - Deve retornar HTTP 200 com todas as atividades")
    void deveRetornar200AoListarTodasAtividades() throws Exception {
        AtividadeEstudanteResponseDto atividadeDto = AtividadeEstudanteResponseDto.builder()
                .id(100L)
                .titulo("Atividade 01")
                .nomeTurma("PSoft")
                .status(StatusAtividade.CONCLUIDA)
                .build();

        when(consultaService.listarAtividades(10L)).thenReturn(List.of(atividadeDto));

        mockMvc.perform(get("/api/estudante/10/atividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/atividades/{atividadeId} - Deve retornar HTTP 200 com detalhes da atividade")
    void deveRetornar200AoBuscarAtividadeEspecifica() throws Exception {
        AtividadeEstudanteResponseDto atividadeDto = AtividadeEstudanteResponseDto.builder()
                .id(100L)
                .titulo("Atividade 01")
                .nomeTurma("PSoft")
                .descricao("Descrição detalhada")
                .status(StatusAtividade.ABERTA)
                .build();

        when(consultaService.buscarAtividade(10L, 100L)).thenReturn(atividadeDto);

        mockMvc.perform(get("/api/estudante/10/atividades/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.descricao").value("Descrição detalhada"));
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/atividades/{atividadeId} - Deve retornar erro quando estudante não pertence à turma")
    void deveRetornarErroQuandoEstudanteNaoPertenceATurma() throws Exception {
        when(consultaService.buscarAtividade(20L, 100L))
                .thenThrow(new BusinessRuleException("O estudante não participa da turma desta atividade"));

        mockMvc.perform(get("/api/estudante/20/atividades/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/atividades - Deve retornar HTTP 404 quando estudante não existe")
    void deveRetornar404QuandoEstudanteNaoExiste() throws Exception {
        when(consultaService.listarAtividades(999L))
                .thenThrow(new ResourceNotFoundException("Estudante não encontrado"));

        mockMvc.perform(get("/api/estudante/999/atividades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/estudante/{id}/atividades/{atividadeId} - Deve retornar HTTP 404 quando atividade não existe")
    void deveRetornar404QuandoAtividadeNaoExiste() throws Exception {
        when(consultaService.buscarAtividade(10L, 999L))
                .thenThrow(new ResourceNotFoundException("Atividade não encontrada com o ID: 999"));

        mockMvc.perform(get("/api/estudante/10/atividades/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}