package br.com.psoft.arena.dto.estrategiaAvaliativa;

import br.com.psoft.arena.model.Estrategia.EstrategiaAvaliacao;
import br.com.psoft.arena.model.Estrategia.TipoEstrategia;
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

    public static EstrategiaAvaliacaoResponseDto fromEntity(EstrategiaAvaliacao e){
        return  EstrategiaAvaliacaoResponseDto.builder()
                .id(e.getId())
                .nome(e.getNome())
                .tipo(e.getTipo())
                .descricao(e.getDescricao())
                .promptAvaliativa(e.getPromptAvaliacao())
                .build(); 

    }
    
}
