package br.com.psoft.arena.services.criptografia;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CriptografiaService")
class CriptografiaServiceTest {

    private static final String CHAVE_MESTRA = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
    private static final String VALOR = "chave-de-api-super-secreta";

    private CriptografiaService criarService() {
        return new CriptografiaService(CHAVE_MESTRA);
    }

    @Test
    @DisplayName("o valor criptografado é diferente do valor original")
    void valorCriptografadoDiferenteDoOriginal() {
        String criptografado = criarService().criptografar(VALOR);

        assertNotEquals(VALOR, criptografado);
        assertFalse(criptografado.contains(VALOR));
    }

    @Test
    @DisplayName("a descriptografia recupera o valor original")
    void descriptografiaRecuperaOriginal() {
        CriptografiaService service = criarService();

        String criptografado = service.criptografar(VALOR);

        assertEquals(VALOR, service.descriptografar(criptografado));
    }

    @Test
    @DisplayName("IVs aleatórios produzem criptogramas diferentes para o mesmo valor")
    void ivAleatorioProduzCriptogramasDiferentes() {
        CriptografiaService service = criarService();

        String primeiro = service.criptografar(VALOR);
        String segundo = service.criptografar(VALOR);

        assertNotEquals(primeiro, segundo);
        assertEquals(VALOR, service.descriptografar(primeiro));
        assertEquals(VALOR, service.descriptografar(segundo));
    }

    @Test
    @DisplayName("sem chave mestra configurada a criptografia falha explicitamente")
    void semChaveMestraFalhaAoCriptografar() {
        CriptografiaService service = new CriptografiaService("");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.criptografar(VALOR));

        assertEquals("A chave mestra de criptografia não está configurada", ex.getMessage());
    }

    @Test
    @DisplayName("sem chave mestra configurada a descriptografia falha explicitamente")
    void semChaveMestraFalhaAoDescriptografar() {
        CriptografiaService service = new CriptografiaService("");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.descriptografar("qualquer-coisa"));

        assertEquals("A chave mestra de criptografia não está configurada", ex.getMessage());
    }

    @Test
    @DisplayName("rejeita chave mestra que não é Base64")
    void rejeitaChaveMestraInvalida() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new CriptografiaService("não-é-base64"));

        assertEquals("A chave mestra de criptografia deve estar codificada em Base64", ex.getMessage());
    }

    @Test
    @DisplayName("rejeita chave mestra que não tenha 32 bytes para AES-256")
    void rejeitaChaveMestraComTamanhoInvalido() {
        String chaveCurta = Base64.getEncoder()
                .encodeToString("chave-curta".getBytes(StandardCharsets.UTF_8));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new CriptografiaService(chaveCurta));

        assertEquals("A chave mestra de criptografia deve ter 32 bytes para AES-256", ex.getMessage());
    }
}
