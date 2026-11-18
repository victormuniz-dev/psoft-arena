package br.com.psoft.arena.services.submissao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.psoft.arena.exception.BusinessRuleException;

class ValidadorCodigoFonteJavaTest {

    private final ValidadorCodigoFonteJava validador = new ValidadorCodigoFonteJava();

    @ParameterizedTest
    @ValueSource(strings = {
        "public class Main { }",
        "interface Forma { double area(); }",
        "public enum Cor { VERMELHO, AZUL }",
        "public record Ponto(int x, int y) { }",
        "package a.b;\nimport java.util.List;\npublic class A {\n  String s = \"}\";\n  char c = '{';\n}"
    })
    void deveAceitarCodigoJava(String codigo) {
        assertThatCode(() -> validador.validar(codigo)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarCodigoNuloOuVazio() {
        assertThatThrownBy(() -> validador.validar(null)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> validador.validar("   ")).isInstanceOf(BusinessRuleException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "print('ola mundo')", "def f(x): return x", "Isto é só um texto qualquer",
        "// class Falsa { }", "/* class Falsa { } */ int x = 1;", "String s = \"class Falsa { }\";"
    })
    void deveRejeitarTextoQueNaoDeclaraTipoJava(String codigo) {
        assertThatThrownBy(() -> validador.validar(codigo))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("em Java");
    }

    @ParameterizedTest
    @ValueSource(strings = { "public class A {", "public class A { } }", "public class A { void m() { } " })
    void deveRejeitarChavesDesbalanceadas(String codigo) {
        assertThatThrownBy(() -> validador.validar(codigo))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("chaves");
    }
}