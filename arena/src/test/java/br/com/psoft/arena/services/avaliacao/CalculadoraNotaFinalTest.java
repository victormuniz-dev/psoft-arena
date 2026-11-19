package br.com.psoft.arena.services.avaliacao;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.psoft.arena.exception.AvaliacaoFalhaException;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;

class CalculadoraNotaFinalTest {

    private CalculadoraNotaFinal calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraNotaFinal();
    }

    // ---------- helpers ----------

    private ConfiguracaoLlmModel llm(String nome) {
        ConfiguracaoLlmModel llm = mock(ConfiguracaoLlmModel.class);
        when(llm.getNomeModelo()).thenReturn(nome);
        return llm;
    }

    private ResultadoLLMModel resultado(String modelo, String nota,
                                        String evidencias, String justificativa) {
        return new ResultadoLLMModel(llm(modelo), new BigDecimal(nota), evidencias, justificativa);
    }

    private ResultadoLLMModel resultado(String nota) {
        return resultado("modelo", nota, "evidência", "justificativa");
    }

    private void assertNota(String esperada, NotaFinal notaFinal) {
        assertEquals(0, new BigDecimal(esperada).compareTo(notaFinal.getNota()),
                "Esperado " + esperada + " mas foi " + notaFinal.getNota());
    }

    // ---------- entradas inválidas ----------

    @Test
    void deveFalharQuandoListaVazia() {
        assertThrows(AvaliacaoFalhaException.class, () -> calculadora.calcular(List.of()));
    }

    @Test
    void deveFalharQuandoListaNula() {
        assertThrows(AvaliacaoFalhaException.class, () -> calculadora.calcular(null));
    }

    // ---------- média ----------

    @Test
    void deveRetornarAPropriaNotaComUmUnicoResultado() {
        assertNota("8.5", calculadora.calcular(List.of(resultado("8.5"))));
    }

    @Test
    void deveCalcularMediaDeDoisResultados() {
        assertNota("7.0", calculadora.calcular(List.of(resultado("8.0"), resultado("6.0"))));
    }
}