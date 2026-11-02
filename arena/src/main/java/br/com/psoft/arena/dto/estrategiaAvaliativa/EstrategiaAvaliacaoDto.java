package br.com.psoft.arena.dto.estrategiaAvaliativa;

import br.com.psoft.arena.model.Estrategia.TipoEstrategia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class EstrategiaAvaliacaoDto {

    @NotBlank (message = "O nome é obriogatório.")
    private String nome; 

    @NotNull  (message = "O tipo de estrategia é obrigatória.")
    private  TipoEstrategia tipo; 

    @NotBlank (message = "A descrição é obrigatória.")
    private  String descricao; 

    @NotBlank (message = "O prompt de avaliação é obrigatória.")
    private  String promptAvaliacao; 
    
}
