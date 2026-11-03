package br.com.psoft.arena.exception;

/**
 * Exceção personalizada para representar casos em que um recurso buscado não é encontrado.
 * Resulta em um retorno HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constrói uma nova ResourceNotFoundException com a mensagem especificada.
     * 
     * @param message A mensagem detalhada da exceção.
     */
    public ResourceNotFoundException(String message){
        super(message);
    }

    /**
     * Constrói uma nova ResourceNotFoundException formatando uma mensagem padrão
     * com base no nome do recurso e no ID fornecido.
     * 
     * @param recurso O nome do tipo de recurso que não foi encontrado.
     * @param id O ID numérico do recurso procurado.
     */
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " não encontrado com id " + id);
    }
}