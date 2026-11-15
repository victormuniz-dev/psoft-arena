package br.com.psoft.arena.dtos.llm;

import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de resposta de uma configuração de LLM.
 * Não expõe a chave da API em nenhuma hipótese.
 */
@Getter
@AllArgsConstructor
public class ConfiguracaoLlmResponseDto {

    private Long id;
    private String nomeModelo;
    private String urlApi;
    private boolean padrao;
    private boolean chaveConfigurada;

    public static ConfiguracaoLlmResponseDto de(ConfiguracaoLlmModel configuracao) {
        if (configuracao == null) {
            return null;
        }

        return new ConfiguracaoLlmResponseDto(
                configuracao.getId(),
                configuracao.getNomeModelo(),
                configuracao.getUrlApi(),
                configuracao.isPadrao(),
                configuracao.getChaveApiCriptografada() != null
                        && !configuracao.getChaveApiCriptografada().isBlank());
    }
}
