package br.com.psoft.arena.models.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.psoft.arena.models.atividade.AtividadeModel;

@DisplayName("ConfiguracaoLlmModel")
class ConfiguracaoLlmModelTest {

    @Test
    @DisplayName("construtor guarda os dados da configuração e a chave criptografada")
    void construtorGuardaDados() {
        ConfiguracaoLlmModel configuracao =
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", "criptografada", true);

        assertNull(configuracao.getId());
        assertEquals("GPT", configuracao.getNomeModelo());
        assertEquals("https://api.exemplo.com", configuracao.getUrlApi());
        assertEquals("criptografada", configuracao.getChaveApiCriptografada());
        assertTrue(configuracao.isPadrao());
        assertNull(configuracao.getAtividade());
    }

    @Test
    @DisplayName("construtor sem argumentos mantém a configuração vazia")
    void construtorSemArgumentos() {
        ConfiguracaoLlmModel configuracao = new ConfiguracaoLlmModel();

        assertNull(configuracao.getNomeModelo());
        assertNull(configuracao.getChaveApiCriptografada());
        assertFalse(configuracao.isPadrao());
        assertNull(configuracao.getAtividade());
    }

    @Test
    @DisplayName("associarAtividade define o vínculo reverso")
    void associarAtividade() {
        ConfiguracaoLlmModel configuracao =
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", "criptografada", true);
        AtividadeModel atividade = new AtividadeModel();

        configuracao.associarAtividade(atividade);

        assertSame(atividade, configuracao.getAtividade());
    }
}
