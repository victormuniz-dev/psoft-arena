package br.com.psoft.arena.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.psoft.arena.exception.LLMIndisponivelException;
import br.com.psoft.arena.exception.RespostaLLMInvalidaException;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;

@Component
public class GrokClient implements LLMClient {

    private static final String BASE_URL = "https://api.x.ai/v1";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public GrokClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this(criarRestClient(builder), objectMapper);
    }

    GrokClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

private static RestClient criarRestClient(RestClient.Builder builder) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(60));
    return builder.baseUrl(BASE_URL).requestFactory(factory).build();
}

    @Override
    public RespostaLLM avaliar(ConfiguracaoLlmModel llm, String prompt) {
        Map<String, Object> body = Map.of(
                "model", llm.getNomeModelo(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        ChatResponse response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + llm.getChaveApiCriptografada())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (RestClientResponseException e) {
            throw new LLMIndisponivelException(
                    "Grok retornou erro HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            // timeout, conexão recusada, DNS etc.
            throw new LLMIndisponivelException("Falha de comunicação com o Grok", e);
        }

        return extrair(response);
    }

    private RespostaLLM extrair(ChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null) {
            throw new LLMIndisponivelException("Grok não respondeu");
        }

        String content = response.choices().get(0).message().content();

        RespostaLLM resposta;
        try {
            resposta = objectMapper.readValue(content, RespostaLLM.class);
        } catch (Exception e) {
            throw new RespostaLLMInvalidaException("Resposta do Grok não é um JSON válido", e);
        }

        if (resposta.nota() == null) {
            throw new RespostaLLMInvalidaException("Resposta do Grok sem nota");
        }
        if (resposta.nota() < 0 || resposta.nota() > 10) {
            throw new RespostaLLMInvalidaException(
                    "Nota fora do intervalo de 0 a 10: " + resposta.nota());
        }
        return resposta;
    }

    private record ChatResponse(List<Choice> choices) {}
    private record Choice(Message message) {}
    private record Message(String content) {}
}
