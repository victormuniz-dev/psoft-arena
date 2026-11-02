package br.com.psoft.arena.controllers.problema;

import br.com.psoft.arena.repositories.problema.ProblemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProblemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProblemaRepository problemaRepository;

    private static final String JSON = """
        {
            "titulo": "Sistema de Biblioteca",
            "descricao": "Sistema para gerenciamento de livros e emprestimos.",
            "requisitosFuncionais": [
                "Cadastrar livros",
                "Registrar emprestimos"
            ],
            "requisitosProjeto": [
                "Utilizar arquitetura MVC",
                "Persistir dados em banco relacional"
            ]
        }
        """;

    @BeforeEach
    void limparBanco() {
        problemaRepository.deleteAll();
    }

    private String cadastrarProblema() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/problemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
            .andExpect(status().isCreated())
            .andReturn();

        return resultado.getResponse().getHeader("Location");
    }

    @Test
    void deveCadastrarProblemaComStatusAtivo() throws Exception {
        mockMvc.perform(post("/problemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.titulo").value("Sistema de Biblioteca"))
            .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveListarProblemasCadastrados() throws Exception {
        cadastrarProblema();

        mockMvc.perform(get("/problemas"))
            .andExpect(status().isOk())
            .andExpect(content().string(
                containsString("Sistema de Biblioteca")
            ));
    }

    @Test
    void deveBuscarProblemaPorId() throws Exception {
        String location = cadastrarProblema();

        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Sistema de Biblioteca"))
            .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveEditarProblema() throws Exception {
        String location = cadastrarProblema();

        String jsonAtualizado = """
            {
                "titulo": "Biblioteca Universitaria",
                "descricao": "Sistema atualizado.",
                "requisitosFuncionais": ["Cadastrar usuarios"],
                "requisitosProjeto": ["Utilizar Spring Boot"]
            }
            """;

        mockMvc.perform(put(location)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonAtualizado))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Biblioteca Universitaria"))
            .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void deveDesativarProblemaEManterInativoAoRepetirOperacao() throws Exception {
        String location = cadastrarProblema();

        mockMvc.perform(patch(location + "/desativar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(patch(location + "/desativar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void deveExcluirProblema() throws Exception {
        String location = cadastrarProblema();

        mockMvc.perform(delete(location))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
            .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarCadastroComTituloVazio() throws Exception {
        String jsonInvalido = """
            {
                "titulo": "",
                "descricao": "Descricao valida.",
                "requisitosFuncionais": ["Cadastrar livros"],
                "requisitosProjeto": ["Utilizar MVC"]
            }
            """;

        mockMvc.perform(post("/problemas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonInvalido))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deveDisponibilizarDocumentacaoOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.info.title").value("PSoft Arena API"));
    }
}
