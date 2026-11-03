package br.com.psoft.arena.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manipulador global de exceções da aplicação.
 * Intercepta exceções lançadas pelos controllers e padroniza as respostas HTTP de erro.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções do tipo {@link ResourceNotFoundException}.
     * 
     * @param e A exceção capturada contendo a mensagem do recurso ausente.
     * @return Um {@link ResponseEntity} com status HTTP 404 (Not Found) e a mensagem de erro.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /**
     * Trata exceções do tipo {@link BusinessRuleException}.
     * 
     * @param e A exceção capturada contendo a regra de negócio violada.
     * @return Um {@link ResponseEntity} com status HTTP 422 e a mensagem de erro.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<String> handleBusinessRule(BusinessRuleException e) {
        return ResponseEntity.status(422).body(e.getMessage());
    }

    /**
     * Trata falhas de validação em argumentos anotados com {@link jakarta.validation.Valid}.
     * 
     * @param e A exceção de validação capturada.
     * @return Um {@link ResponseEntity} com status HTTP 400 (Bad Request) contendo a mensagem do primeiro erro de validação.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(message);
    }
}