package br.com.psoft.arena.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Set;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.List;

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
import br.com.psoft.arena.services.criptografia.CriptografiaService;

/**
 * Cliente HTTP para chamar LLMs compatíveis com a API de chat completions.
 *
 * Cada {@link ConfiguracaoLlmModel} contém a URL‑base da API e a chave API criptografada.
 * O cliente valida a URL, descriptografa a chave e a envia via cabeçalho Authorization.
 */
@Component
public class GrokClient implements LLMClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CriptografiaService criptografiaService;
    private static final Set<String> PRIVATE_HOSTNAMES = Set.of(
            "localhost"
    );

    private void validateUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("URL da API LLM não pode ser nula ou vazia");
        }
        URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL da API LLM inválida", e);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("URL da API LLM deve usar HTTPS");
        }
        if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("URL da API LLM não deve conter credenciais, query ou fragment");
        }
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("URL da API LLM sem host");
        }
        // Directly reject known private hostnames.
        if (PRIVATE_HOSTNAMES.contains(host.toLowerCase())) {
            throw new IllegalArgumentException("URL da API LLM aponta para hostname privado ou localhost");
        }
        // Resolve all IPs for the hostname and validate each.
        InetAddress[] addresses;
        try {
            addresses = resolveAll(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("URL da API LLM não pôde ser resolvida", e);
        }
        for (InetAddress addr : addresses) {
            if (isProhibited(addr)) {
                throw new IllegalArgumentException("URL da API LLM resolve para endereço interno ou reservado: " + addr.getHostAddress());
            }
        }
    }

    /** Allows overriding in tests. */
    protected InetAddress[] resolveAll(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    private boolean isProhibited(InetAddress addr) {
        // Reject loopback, any-local, link‑local, site‑local (private), multicast, and unspecified.
        return addr.isLoopbackAddress()
                || addr.isAnyLocalAddress()
                || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress()
                || addr.isMulticastAddress();
    }


    @Autowired
    public GrokClient(CriptografiaService criptografiaService) {
        this(criarRestClient(RestClient.builder()),
                new ObjectMapper(),
                criptografiaService);
    }

    // Constructor used in tests to supply a pre‑built RestClient and mocks.
    GrokClient(RestClient restClient, ObjectMapper objectMapper, CriptografiaService criptografiaService) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.criptografiaService = criptografiaService;
    }

    private static RestClient criarRestClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));
        // No base URL – o endpoint completo será definido por cada {@link ConfiguracaoLlmModel}.
        return builder.requestFactory(factory).build();
    }

    @Override
    public RespostaLLM avaliar(ConfiguracaoLlmModel llm, String prompt) {
        // ---------- validação da URL ----------
        String baseUrl = llm.getUrlApi();
        validateUrl(baseUrl);

        // ---------- descriptografia da chave ----------
        String apiKey;
        try {
            apiKey = criptografiaService.descriptografar(llm.getChaveApiCriptografada());
        } catch (Exception e) {
            throw new LLMIndisponivelException("Falha ao descriptografar chave API", e);
        }

        // Monta URL completa para chat completions.
        String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        Map<String, Object> body = Map.of(
                "model", llm.getNomeModelo(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        ChatResponse response;
        try {
            response = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (RestClientResponseException e) {
            throw new LLMIndisponivelException(
                    "Grok retornou erro HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
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
