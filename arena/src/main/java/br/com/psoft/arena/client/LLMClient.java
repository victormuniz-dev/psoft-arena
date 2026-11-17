package br.com.psoft.arena.client;

import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;

public interface LLMClient {
    RespostaLLM avaliar(ConfiguracaoLlmModel llm, String prompt);
}
