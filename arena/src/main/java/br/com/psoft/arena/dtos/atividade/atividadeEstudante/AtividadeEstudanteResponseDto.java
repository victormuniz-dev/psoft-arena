package br.com.psoft.arena.dtos.atividade.atividadeEstudante;

import java.time.LocalDateTime;
import java.util.List;

import br.com.psoft.arena.dtos.problema.ProblemaResponseDto;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data 
@Builder
@AllArgsConstructor
@NoArgsConstructor  
public class AtividadeEstudanteResponseDto {
   
    private Long id; 
    private String titulo;
    private String nomeTurma; 
    private String codigo; 
    private String descricao;  
    private LocalDateTime prazo;  
    private StatusAtividade status;
    private List<ProblemaResponseDto> problemas; 
    private List<String> estrategiaAvaliativa; 

    public  static AtividadeEstudanteResponseDto fromEntity(AtividadeModel atividade){
        List<ProblemaResponseDto> problemaDto = atividade.getProblemas().stream()
        .map(ProblemaResponseDto::from)
        .toList(); 

        List<String> estrategiaDto = atividade.getEstrategias().stream()
                                    .map(EstrategiaAvaliacaoModel::getNome)
                                    .toList(); 

        return AtividadeEstudanteResponseDto.builder()
               .id(atividade.getId())
               .titulo(atividade.getTitulo())
               .codigo(atividade.getTurma().getCodigo())
               .nomeTurma(atividade.getTurma().getNome())
               .descricao(atividade.getDescricao())
               .prazo(atividade.getPrazo())
               .status(atividade.getStatus())
               .problemas(problemaDto)
               .estrategiaAvaliativa(estrategiaDto)
               .build(); 
    }

    
}
