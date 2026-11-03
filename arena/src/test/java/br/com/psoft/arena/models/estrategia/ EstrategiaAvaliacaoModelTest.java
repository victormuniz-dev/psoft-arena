package br.com.psoft.arena.models.estrategia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testes Unitários - EstrategiaAvaliacaoModel")
class EstrategiaAvaliacaoModelTest {

    @Test
    @DisplayName("Deve instanciar a entidade corretamente utilizando o Builder")
    void deveCriarModelViaBuilder() {
        EstrategiaAvaliacaoModel model = EstrategiaAvaliacaoModel.builder()
                .id(1L)
                .nome("Análise de Smells")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Verifica ocorrência de code smells")
                .promptAvaliacao("Instrução do prompt")
                .build();

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(1L);
        assertThat(model.getNome()).isEqualTo("Análise de Smells");
        assertThat(model.getTipo()).isEqualTo(TipoEstrategia.CODE_SMELL);
        assertThat(model.getDescricao()).isEqualTo("Verifica ocorrência de code smells");
        assertThat(model.getPromptAvaliacao()).isEqualTo("Instrução do prompt");
    }

    @Test
    @DisplayName("Deve alterar e obter atributos corretamente via Getters e Setters")
    void deveAlterarAtributosViaGettersESetters() {
        EstrategiaAvaliacaoModel model = new EstrategiaAvaliacaoModel();

        model.setId(2L);
        model.setNome("Padrão de Projeto");
        model.setTipo(TipoEstrategia.PADRAO_DE_PROJETO);
        model.setDescricao("Verifica padrões GoF");
        model.setPromptAvaliacao("Novo prompt");

        assertThat(model.getId()).isEqualTo(2L);
        assertThat(model.getNome()).isEqualTo("Padrão de Projeto");
        assertThat(model.getTipo()).isEqualTo(TipoEstrategia.PADRAO_DE_PROJETO);
        assertThat(model.getDescricao()).isEqualTo("Verifica padrões GoF");
        assertThat(model.getPromptAvaliacao()).isEqualTo("Novo prompt");
    }
}
