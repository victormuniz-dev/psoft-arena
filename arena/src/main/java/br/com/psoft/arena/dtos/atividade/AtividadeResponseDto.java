package br.com.psoft.arena.dtos.atividade;

import java.util.List;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AtividadeResponseDto {

    private Long id;
    private String titulo;
    private Long turmaId;
    private List<Long> problemaIds;
    private List<Long> estrategiaIds;

    public static AtividadeResponseDto de(AtividadeModel a) {
        return new AtividadeResponseDto(
                a.getId(),
                a.getTitulo(),
                a.getTurma().getId(),
                a.getProblemas().stream().map(ProblemaModel::getId).toList(),
                a.getEstrategias().stream().map(EstrategiaAvaliacaoModel::getId).toList());
    }
}