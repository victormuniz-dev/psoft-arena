package br.com.psoft.arena.dtos.llm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;

import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("ConfiguracaoLlmResponseDto")
class ConfiguracaoLlmResponseDtoTest {

    private static final String CHAVE_API = "chave-api-secreta";
    private static final String CHAVE_CRIPTOGRAFADA = "Y2hhdmUtY3JpcHRvZ3JhZGE=";

    @Test
    @DisplayName("de(null) retorna null")
    void deNuloRetornaNulo() {
        assertNull(ConfiguracaoLlmResponseDto.de(null));
    }

    @Test
    @DisplayName("expõe apenas os dados não sensíveis e indica que a chave está configurada")
    void expoeApenasDadosNaoSensiveis() {
        ConfiguracaoLlmModel model =
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", CHAVE_CRIPTOGRAFADA, true);
        ReflectionTestUtils.setField(model, "id", 3L);

        ConfiguracaoLlmResponseDto dto = ConfiguracaoLlmResponseDto.de(model);

        assertTrue(dto.isPadrao());
        assertTrue(dto.isChaveConfigurada());
    }

    @Test
    @DisplayName("a serialização não contém a chave original nem a criptografada")
    void serializacaoNaoExpoeChave() throws Exception {
        ConfiguracaoLlmModel model =
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", CHAVE_CRIPTOGRAFADA, true);

        String json = new ObjectMapper().writeValueAsString(ConfiguracaoLlmResponseDto.de(model));

        assertFalse(json.contains(CHAVE_API));
        assertFalse(json.contains(CHAVE_CRIPTOGRAFADA));
    }

    @Test
    @DisplayName("chaveConfigurada é false quando não há chave armazenada")
    void chaveNaoConfigurada() {
        ConfiguracaoLlmModel model = new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", " ", false);

        ConfiguracaoLlmResponseDto dto = ConfiguracaoLlmResponseDto.de(model);

        assertFalse(dto.isChaveConfigurada());
    }
}
