package br.com.psoft.arena.exception;

public class RespostaLLMInvalidaException extends AvaliacaoFalhaException{
   public RespostaLLMInvalidaException(String message) {
        super(message);
    }

    public RespostaLLMInvalidaException(String message, Throwable cause) {
        super(message, cause);
    } 
}
