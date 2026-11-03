package br.com.psoft.arena.controllers.estrategia;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dtos.estrategia.PromptUpdateDto;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;

@WebMvcTest(EstrategiaAvaliacaoController.class)
@AutoConfigureMockMvc
@DisplayName("Testes de Controller - EstrategiaAvaliacaoController")
class EstrategiaAvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean 
    private EstrategiaAvaliacaoService service;

    private EstrategiaAvaliacaoResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = EstrategiaAvaliacaoResponseDto.builder()
                .id(1L)
                .nome("Estratégia Teste")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Descrição teste")
                .promptAvaliativa("Prompt teste")
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar nova estratégia com sucesso (HTTP 201)")
    void deveCadastrarComSucesso() throws Exception {
        EstrategiaAvaliacaoDto inputDto = new EstrategiaAvaliacaoDto("Estratégia Teste", TipoEstrategia.CODE_SMELL, "Descrição teste", "Prompt teste");

        when(service.cadastrar(any(EstrategiaAvaliacaoDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/estrategia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Estratégia Teste"));
    }

    @Test
    @DisplayName("Deve buscar estratégia por ID com sucesso (HTTP 200)")
    void deveBuscarPorIdComSucesso() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/estrategia/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Estratégia Teste"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 404 ao buscar ID inexistente")
    void deveRetornar404AoBuscarIdInexistente() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Estratégia de Avaliação", 99L));

        mockMvc.perform(get("/api/estrategia/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve listar todas as estratégias (HTTP 200)")
    void deveListarTodas() throws Exception {
        when(service.liestarTodos()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/estrategia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("Deve atualizar o prompt da estratégia com sucesso (HTTP 200)")
    void deveAtualizarPromptComSucesso() throws Exception {
        PromptUpdateDto promptDto = new PromptUpdateDto("Novo prompt atualizado");

        when(service.atualizarPromt(eq(1L), any(PromptUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/estrategia/1/prompt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(promptDto)))
                .andExpect(status().isOk());
    }
}