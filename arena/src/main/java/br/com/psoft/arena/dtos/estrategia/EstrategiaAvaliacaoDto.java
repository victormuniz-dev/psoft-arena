package br.com.psoft.arena.dtos.estrategia;

import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferência de dados de criação e atualização de uma Estratégia de Avaliação.
 * Contém os campos necessários recebidos do cliente nas requisições HTTP.
 */
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class EstrategiaAvaliacaoDto {

    /**
     * Nome descritivo da estratégia de avaliação.
     */
    @NotBlank (message = "O nome é obriogatório.")
    private String nome; 

    /**
     * Tipo da estratégia de avaliação.
     */
    @NotNull  (message = "O tipo de estrategia é obrigatória.")
    private  TipoEstrategia tipo; 

    
    /**
     * Descrição da estrategia.
     */
    @NotBlank (message = "A descrição é obrigatória.")
    private  String descricao; 
    
    /**
     * Prompt de instrução associado à estratégia.
     */
    @NotBlank (message = "O prompt de avaliação é obrigatória.")
    private  String promptAvaliacao; 
    
}