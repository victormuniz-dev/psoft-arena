package br.com.psoft.arena.dtos.estrategia;

import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class EstrategiaAvaliacaoResponseDto {

    private Long id; 
    private String nome; 
    private TipoEstrategia tipo; 
    private String descricao; 
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