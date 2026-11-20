package br.com.psoft.arena.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.psoft.arena.exception.AvaliacaoFalhaException;
import br.com.psoft.arena.exception.LLMIndisponivelException;
import br.com.psoft.arena.exception.RespostaLLMInvalidaException;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;

class GrokClientTest {

    private static final String URL = "https://api.x.ai/v1/chat/completions";
    private static final String PROMPT = "avalie este código";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockRestServiceServer server;
    private GrokClient client;
    private ConfiguracaoLlmModel llm;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.x.ai/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GrokClient(builder.build(), objectMapper);

        llm = mock(ConfiguracaoLlmModel.class);
        when(llm.getNomeModelo()).thenReturn("grok-3");
        when(llm.getChaveApiCriptografada()).thenReturn("chave-secreta");
    }

    // ---------- helpers ----------

    /** Monta a resposta do Grok com o conteúdo (já serializado) dentro de choices[0].message.content. */
    private String chatResponse(String content) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", content)))));
    }

    private void responder(String corpo) {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(corpo, MediaType.APPLICATION_JSON));
    }

    private void responderComConteudo(String conteudo) throws Exception {
        responder(chatResponse(conteudo));
    }

    // ---------- sucesso ----------

    @Test
    void deveExtrairNotaEvidenciasEJustificativa() throws Exception {
        responderComConteudo("""
                {"nota": 8.5, "evidencias": "classe X usa interface", "justificativa": "bom design"}
                """);

        RespostaLLM resposta = client.avaliar(llm, PROMPT);

        assertEquals(8.5, resposta.nota());
        assertEquals("classe X usa interface", resposta.evidencias());
        assertEquals("bom design", resposta.justificativa());
        server.verify();
    }

    @Test
    void deveEnviarRequisicaoNoFormatoEsperado() throws Exception {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer chave-secreta"))
                .andExpect(jsonPath("$.model").value("grok-3"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value(PROMPT))
                .andExpect(jsonPath("$.response_format.type").value("json_object"))
                .andRespond(withSuccess(
                        chatResponse("{\"nota\": 7, \"evidencias\": \"e\", \"justificativa\": \"j\"}"),
                        MediaType.APPLICATION_JSON));

        client.avaliar(llm, PROMPT);

        server.verify();
    }

    @Test
    void deveAceitarLimitesZeroEDez() throws Exception {
        responderComConteudo("{\"nota\": 0, \"evidencias\": \"e\", \"justificativa\": \"j\"}");
        assertEquals(0.0, client.avaliar(llm, PROMPT).nota());

        server.reset();
        responderComConteudo("{\"nota\": 10, \"evidencias\": \"e\", \"justificativa\": \"j\"}");
        assertEquals(10.0, client.avaliar(llm, PROMPT).nota());
    }

    @Test
    void deveIgnorarCamposExtrasNoJson() throws Exception {
        responderComConteudo(
                "{\"nota\": 6, \"evidencias\": \"e\", \"justificativa\": \"j\", \"extra\": \"x\"}");

        assertEquals(6.0, client.avaliar(llm, PROMPT).nota());
    }

    // ---------- falhas de comunicação: LLMIndisponivelException ----------

    @Test
    void deveLancarIndisponivelQuandoHttp401() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        LLMIndisponivelException e = assertThrows(LLMIndisponivelException.class,
                () -> client.avaliar(llm, PROMPT));

        assertEquals("Grok retornou erro HTTP 401", e.getMessage());
    }

    @Test
    void deveLancarIndisponivelQuandoHttp500() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        LLMIndisponivelException e = assertThrows(LLMIndisponivelException.class,
                () -> client.avaliar(llm, PROMPT));

        assertEquals("Grok retornou erro HTTP 500", e.getMessage());
    }

    @Test
    void deveLancarIndisponivelQuandoHttp429() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(LLMIndisponivelException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarIndisponivelQuandoFalhaDeConexao() {
        server.expect(requestTo(URL))
                .andRespond(request -> { throw new IOException("conexão recusada"); });

        LLMIndisponivelException e = assertThrows(LLMIndisponivelException.class,
                () -> client.avaliar(llm, PROMPT));

        assertEquals("Falha de comunicação com o Grok", e.getMessage());
    }

    @Test
    void deveLancarIndisponivelQuandoCorpoVazio() {
        responder("{}");

        assertThrows(LLMIndisponivelException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarIndisponivelQuandoChoicesVazio() {
        responder("{\"choices\": []}");

        assertThrows(LLMIndisponivelException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarIndisponivelQuandoMessageNula() {
        responder("{\"choices\": [{\"message\": null}]}");

        assertThrows(LLMIndisponivelException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarIndisponivelQuandoContentNulo() {
        responder("{\"choices\": [{\"message\": {\"content\": null}}]}");

        assertThrows(LLMIndisponivelException.class, () -> client.avaliar(llm, PROMPT));
    }

    // ---------- resposta inválida: RespostaLLMInvalidaException ----------

    @Test
    void deveLancarInvalidaQuandoConteudoNaoEJson() throws Exception {
        responderComConteudo("isto não é json");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarInvalidaQuandoJsonMalformado() throws Exception {
        responderComConteudo("{\"nota\": 8, \"evidencias\": ");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarInvalidaQuandoNotaAusente() throws Exception {
        responderComConteudo("{\"evidencias\": \"e\", \"justificativa\": \"j\"}");

        RespostaLLMInvalidaException e = assertThrows(RespostaLLMInvalidaException.class,
                () -> client.avaliar(llm, PROMPT));

        assertEquals("Resposta do Grok sem nota", e.getMessage());
    }

    @Test
    void deveLancarInvalidaQuandoNotaNula() throws Exception {
        responderComConteudo("{\"nota\": null, \"evidencias\": \"e\", \"justificativa\": \"j\"}");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarInvalidaQuandoNotaNegativa() throws Exception {
        responderComConteudo("{\"nota\": -0.1, \"evidencias\": \"e\", \"justificativa\": \"j\"}");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarInvalidaQuandoNotaMaiorQueDez() throws Exception {
        responderComConteudo("{\"nota\": 10.1, \"evidencias\": \"e\", \"justificativa\": \"j\"}");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveLancarInvalidaQuandoNotaNaoENumerica() throws Exception {
        responderComConteudo("{\"nota\": \"alta\", \"evidencias\": \"e\", \"justificativa\": \"j\"}");

        assertThrows(RespostaLLMInvalidaException.class, () -> client.avaliar(llm, PROMPT));
    }

    // ---------- hierarquia ----------

    @Test
    void falhasDevemSerAvaliacaoFalhaException() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        // o processador captura só a classe base
        assertThrows(AvaliacaoFalhaException.class, () -> client.avaliar(llm, PROMPT));
    }

    @Test
    void deveManterACausaOriginalNaExcecaoHttp() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        LLMIndisponivelException e = assertThrows(LLMIndisponivelException.class,
                () -> client.avaliar(llm, PROMPT));

        assertInstanceOf(org.springframework.web.client.RestClientResponseException.class, e.getCause());
    }
}