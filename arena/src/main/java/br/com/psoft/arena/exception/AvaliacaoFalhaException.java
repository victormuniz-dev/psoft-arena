package br.com.psoft.arena.exception;

public class AvaliacaoFalhaException  extends RuntimeException{
    public AvaliacaoFalhaException(String message){
        super(message);
    }

    public AvaliacaoFalhaException(String message, Throwable cause){
        super(message, cause);
    }
}
