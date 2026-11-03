package br.com.psoft.arena.dtos.estrategia;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 /**
 * DTO de requisição dedicado para atualização isolada do prompt de uma estratégia.
 */
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class PromptUpdateDto {

    /**
     * Novo texto de prompt a ser associado à estratégia.
     */
    @NotBlank (message = "O novo prompt de avaliação não pode ser vazia.")
    private String promptAvaliacao; 
    
}
