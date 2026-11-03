package br.com.psoft.arena.exception;

/**
 * Exceção personalizada para representar violações de regras de negócio na aplicação.
 * É tratada globalmente retornando o código de status HTTP adequado (422 Unprocessable Entity).
 */
public class BusinessRuleException extends RuntimeException {
    
    /**
     * Constrói uma nova BusinessRuleException com a mensagem detalhada especificada.
     * 
     * @param message A mensagem detalhada explicando o motivo da violação de regra de negócio.
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}