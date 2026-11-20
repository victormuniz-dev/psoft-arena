package br.com.psoft.arena.exception;

public class ConfiguracaoLlmInvalidaException extends RuntimeException{
    public ConfiguracaoLlmInvalidaException(String mensagem){
        super(mensagem);
    }
}
