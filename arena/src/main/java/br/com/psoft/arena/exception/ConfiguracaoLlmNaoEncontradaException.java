package br.com.psoft.arena.exception;

public class ConfiguracaoLlmNaoEncontradaException extends RuntimeException{
    public ConfiguracaoLlmNaoEncontradaException(Long id){
        super("Configuração de LLM não encontrada: " + id);
    }
}
