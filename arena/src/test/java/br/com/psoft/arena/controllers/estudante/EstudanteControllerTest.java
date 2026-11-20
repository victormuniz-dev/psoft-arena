package br.com.psoft.arena.controllers.estudante;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.psoft.arena.dtos.estudante.EstudanteRequestDTO;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.GlobalExceptionHandler;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.services.estudante.EstudanteService;

@WebMvcTest(EstudanteController.class)
@Import(GlobalExceptionHandler.class)
public class EstudanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EstudanteService service;

    private EstudanteModel estudante;

    @BeforeEach
    void setUp() {
        estudante = new EstudanteModel("Maria Silva", "20231001", "maria@ccc.ufcg.edu.br");
        ReflectionTestUtils.setField(estudante, "id", 1L);
    }

    @Test
    @DisplayName("POST /estudantes - Deve retornar 201 Created ao criar estudante válido")
    void deveCriarEstudanteComSucesso() throws Exception {
        EstudanteRequestDTO request = new EstudanteRequestDTO("Maria Silva", "20231001", "maria@ccc.ufcg.edu.br");

        when(service.criar(any(EstudanteRequestDTO.class))).thenReturn(estudante);

        mockMvc.perform(post("/estudantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.matricula").value("20231001"))
                .andExpect(jsonPath("$.email").value("maria@ccc.ufcg.edu.br"));
    }

    @Test
    @DisplayName("POST /estudantes - Deve retornar 400 Bad Request quando dados forem inválidos")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        EstudanteRequestDTO requestInvalido = new EstudanteRequestDTO("", "", "email-invalido");

        mockMvc.perform(post("/estudantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /estudantes - Deve retornar 422 Unprocessable Entity quando matrícula já existir")
    void deveRetornar422QuandoMatriculaDuplicada() throws Exception {
        EstudanteRequestDTO request = new EstudanteRequestDTO("Maria Silva", "20231001", "maria@ccc.ufcg.edu.br");

        when(service.criar(any(EstudanteRequestDTO.class)))
                .thenThrow(new BusinessRuleException("Já existe um estudante com a matrícula 20231001"));

        mockMvc.perform(post("/estudantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("GET /estudantes - Deve retornar 200 OK com lista de estudantes")
    void deveListarEstudantes() throws Exception {
        when(service.listar()).thenReturn(List.of(estudante));

        mockMvc.perform(get("/estudantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("GET /estudantes/{id} - Deve retornar 200 OK ao buscar estudante existente")
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(estudante);

        mockMvc.perform(get("/estudantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.matricula").value("20231001"));
    }

    @Test
    @DisplayName("GET /estudantes/{id} - Deve retornar 404 Not Found quando estudante não existir")
    void deveRetornar404QuandoIdNaoExiste() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Estudante não encontrado com id 99"));

        mockMvc.perform(get("/estudantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /estudantes/matricula/{matricula} - Deve retornar 200 OK ao buscar por matrícula")
    void deveBuscarPorMatricula() throws Exception {
        when(service.buscarPorMatricula("20231001")).thenReturn(estudante);

        mockMvc.perform(get("/estudantes/matricula/20231001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    @DisplayName("PUT /estudantes/{id} - Deve retornar 200 OK ao editar estudante")
    void deveEditarEstudante() throws Exception {
        EstudanteRequestDTO request = new EstudanteRequestDTO("Maria Editada", "20231001", "maria.edit@ccc.ufcg.edu.br");
        EstudanteModel atualizado = new EstudanteModel("Maria Editada", "20231001", "maria.edit@ccc.ufcg.edu.br");
        ReflectionTestUtils.setField(atualizado, "id", 1L);

        when(service.editar(eq(1L), any(EstudanteRequestDTO.class))).thenReturn(atualizado);

        mockMvc.perform(put("/estudantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Editada"))
                .andExpect(jsonPath("$.email").value("maria.edit@ccc.ufcg.edu.br"));
    }
}