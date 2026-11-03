package br.com.psoft.arena.dtos.estrategia;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class PromptUpdateDto {

    @NotBlank (message = "O novo prompt de avaliação não pode ser vazia.")
    private String promptAvaliacao; 
    
}
