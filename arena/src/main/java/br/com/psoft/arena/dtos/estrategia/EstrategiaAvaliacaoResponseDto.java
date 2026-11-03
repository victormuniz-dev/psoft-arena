package br.com.psoft.arena.dtos.estrategia;

import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta enviado nas requisições completadas com sucesso.
 * Retorna o estado atualizado da estratégia de avaliação para o cliente.
 */
@Data 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class EstrategiaAvaliacaoResponseDto {
    
    /**
     * Identificador único da estratégia no banco de dados.
     */
    private Long id; 

    /**
     * Nome cadastrado para a estratégia.
     */
    private String nome; 
    /**
     * Tipo da estratégia associada.
     */
    private TipoEstrategia tipo; 

    /**
     * descrição da estratégia
     */
    private String descricao; 

    /**
     * Prompt configurado para a estratégia.
     */
    private String promptAvaliativa; 

    public static EstrategiaAvaliacaoResponseDto fromEntity(EstrategiaAvaliacaoModel e){
        return  EstrategiaAvaliacaoResponseDto.builder()
                .id(e.getId())
                .nome(e.getNome())
                .tipo(e.getTipo())
                .descricao(e.getDescricao())
                .promptAvaliativa(e.getPromptAvaliacao())
                .build(); 

    }
    
}