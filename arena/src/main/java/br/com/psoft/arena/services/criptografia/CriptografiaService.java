package br.com.psoft.arena.services.criptografia;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por criptografar e descriptografar dados sensíveis
 * utilizando AES/GCM/NoPadding.
 */
@Service
public class CriptografiaService {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG = 128;
    private static final int TAMANHO_CHAVE_AES_256 = 32;

    private final byte[] chaveMestra;

    public CriptografiaService(
            @Value("${app.criptografia.chave-mestra-base64:}") String chaveMestraBase64) {
        this.chaveMestra = decodificarChaveMestra(chaveMestraBase64);
    }

    /**
     * Criptografa o valor informado. O resultado contém o IV aleatório
     * concatenado ao texto cifrado, codificado em Base64.
     *
     * @param valor Texto puro a ser criptografado.
     * @return Texto cifrado em Base64.
     */
    public String criptografar(String valor) {
        validarChaveMestra();

        try {
            byte[] iv = new byte[TAMANHO_IV];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(chaveMestra, "AES"),
                    new GCMParameterSpec(TAMANHO_TAG, iv));

            byte[] textoCifrado = cipher.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            byte[] resultado = new byte[iv.length + textoCifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(textoCifrado, 0, resultado, iv.length, textoCifrado.length);

            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível criptografar a chave da API", e);
        }
    }

    /**
     * Descriptografa um valor produzido por {@link #criptografar(String)}.
     *
     * @param valorCriptografado Texto cifrado em Base64.
     * @return Texto puro original.
     */
    public String descriptografar(String valorCriptografado) {
        validarChaveMestra();

        try {
            byte[] dados = Base64.getDecoder().decode(valorCriptografado);
            if (dados.length <= TAMANHO_IV) {
                throw new IllegalArgumentException("Conteúdo criptografado inválido");
            }

            byte[] iv = new byte[TAMANHO_IV];
            byte[] textoCifrado = new byte[dados.length - TAMANHO_IV];
            System.arraycopy(dados, 0, iv, 0, TAMANHO_IV);
            System.arraycopy(dados, TAMANHO_IV, textoCifrado, 0, textoCifrado.length);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(chaveMestra, "AES"),
                    new GCMParameterSpec(TAMANHO_TAG, iv));

            return new String(cipher.doFinal(textoCifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível descriptografar a chave da API", e);
        }
    }

    private byte[] decodificarChaveMestra(String chaveMestraBase64) {
        if (chaveMestraBase64 == null || chaveMestraBase64.isBlank()) {
            return null;
        }

        try {
            byte[] chave = Base64.getDecoder().decode(chaveMestraBase64.trim());
            if (chave.length != TAMANHO_CHAVE_AES_256) {
                throw new IllegalStateException(
                        "A chave mestra de criptografia deve ter 32 bytes para AES-256");
            }
            return chave;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "A chave mestra de criptografia deve estar codificada em Base64", e);
        }
    }

    private void validarChaveMestra() {
        if (chaveMestra == null) {
            throw new IllegalStateException(
                    "A chave mestra de criptografia não está configurada");
        }
    }
}
