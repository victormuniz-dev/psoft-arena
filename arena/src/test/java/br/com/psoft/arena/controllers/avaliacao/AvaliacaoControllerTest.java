package br.com.psoft.arena.controllers.avaliacao;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.psoft.arena.dtos.avaliacao.ResultadoAvaliacaoResponseDto;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.services.avaliacao.AvaliacaoService;

@WebMvcTest(AvaliacaoController.class)
@AutoConfigureMockMvc
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @Test
    void deveConsultarResultadoDaPropriaSubmissao() throws Exception {
        ResultadoAvaliacaoResponseDto resultado =
            new ResultadoAvaliacaoResponseDto(
                10L,
                StatusSubmissao.CONCLUIDA,
                true,
                "Resultado da avaliação disponível.",
                java.time.LocalDateTime.now(),
                new BigDecimal("8.5"),
                "Código coeso",
                "Boa estrutura",
                List.of()
            );

        when(avaliacaoService.consultarResultado(1L, 10L))
            .thenReturn(resultado);

        mockMvc.perform(get(
                "/submissoes/estudante/1/10/resultado"
            ).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.submissaoId").value(10))
            .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
            .andExpect(jsonPath("$.disponivel").value(true))
            .andExpect(jsonPath("$.nota").value(8.5))
            .andExpect(jsonPath("$.justificativa").value("Código coeso"))
            .andExpect(jsonPath("$.evidencias").value("Boa estrutura"));
    }

    @Test
    void deveInformarQuandoResultadoAindaNaoEstiverDisponivel() throws Exception {
        ResultadoAvaliacaoResponseDto resultado =
            new ResultadoAvaliacaoResponseDto(
                10L,
                StatusSubmissao.PROCESSANDO,
                false,
                "A avaliação ainda não foi concluída.",
                null,
                null,
                null,
                null,
                List.of()
            );

        when(avaliacaoService.consultarResultado(1L, 10L))
            .thenReturn(resultado);

        mockMvc.perform(get(
                "/submissoes/estudante/1/10/resultado"
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.disponivel").value(false))
            .andExpect(jsonPath("$.situacao").value("PROCESSANDO"))
            .andExpect(jsonPath("$.nota").doesNotExist())
            .andExpect(jsonPath("$.mensagem").exists());
    }
}
