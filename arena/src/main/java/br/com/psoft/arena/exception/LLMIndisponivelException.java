package br.com.psoft.arena.exception;

public class LLMIndisponivelException extends AvaliacaoFalhaException{
     public LLMIndisponivelException(String message) {
        super(message);
    }

    public LLMIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
