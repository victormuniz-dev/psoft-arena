package br.com.psoft.arena.dtos.problema;

import java.util.List;

import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.problema.StatusProblema;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProblemaResponseDto {

    private Long id;
    private String titulo;
    private String descricao;
    private List<String> requisitosFuncionais;
    private List<String> requisitosProjeto;
    private StatusProblema status;

    public static ProblemaResponseDto from(ProblemaModel problema) {
        return new ProblemaResponseDto(
            problema.getId(),
            problema.getTitulo(),
            problema.getDescricao(),
            problema.getRequisitosFuncionais(),
            problema.getRequisitosProjeto(),
            problema.getStatus()
        );
    }
}
