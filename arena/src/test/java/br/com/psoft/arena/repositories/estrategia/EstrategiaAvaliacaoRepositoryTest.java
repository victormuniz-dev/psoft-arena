package br.com.psoft.arena.repositories.estrategia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;

@DataJpaTest
@DisplayName("Testes de Integração/Persistência do EstrategiaAvaliacaoRepository")
class EstrategiaAvaliacaoRepositoryTest {

    @Autowired
    private EstrategiaAvaliacaoRepository repository;

    private EstrategiaAvaliacaoModel estrategiaCodeSmell;
    private EstrategiaAvaliacaoModel estrategiaPadraoProjeto;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        estrategiaCodeSmell = EstrategiaAvaliacaoModel.builder()
                .nome("Avaliação de Smells")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Verifica ocorrência de code smells")
                .promptAvaliacao("Prompt para code smells")
                .build();

        estrategiaPadraoProjeto = EstrategiaAvaliacaoModel.builder()
                .nome("Avaliação de Padrões")
                .tipo(TipoEstrategia.PADRAO_DE_PROJETO)
                .descricao("Verifica uso de padrões GoF")
                .promptAvaliacao("Prompt para padrões")
                .build();
    }

    @Test
    @DisplayName("Deve buscar estratégias por tipo com sucesso")
    void deveBuscarEstrategiasPorTipoComSucesso() {
        repository.save(estrategiaCodeSmell);
        repository.save(estrategiaPadraoProjeto);

        List<EstrategiaAvaliacaoModel> resultado = repository.findByTipo(TipoEstrategia.CODE_SMELL);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Avaliação de Smells");
        assertThat(resultado.get(0).getTipo()).isEqualTo(TipoEstrategia.CODE_SMELL);
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao buscar por tipo inexistente no banco")
    void deveRetornarListaVaziaQuandoTipoNaoExistir() {
        repository.save(estrategiaCodeSmell);

        List<EstrategiaAvaliacaoModel> resultado = repository.findByTipo(TipoEstrategia.PADRAO_DE_PROJETO);

        assertThat(resultado).isEmpty();
    }
}