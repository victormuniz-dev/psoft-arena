package br.com.psoft.arena.dtos.atividade;

import java.util.List;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de resposta contendo os dados de uma atividade visíveis ao estudante (US7).
 * Omite informações internas de configuração do professor, como credenciais de API do LLM.
 */
@Getter
@AllArgsConstructor
public class AtividadeResponseDto {

    private Long id;
    private String titulo;
    private Long turmaId;
    private List<Long> problemaIds;
    private List<Long> estrategiaIds;


    /**
     * Converte uma entidade {@link AtividadeModel} em um {@link AtividadeEstudanteResponseDto}.
     *
     * @param atividade Entidade de domínio da atividade.
     * @return Instância preenchida do DTO com os problemas e estratégias associadas.
     * */
    public static AtividadeResponseDto de(AtividadeModel a) {
        return new AtividadeResponseDto(
                a.getId(),
                a.getTitulo(),
                a.getTurma().getId(),
                a.getProblemas().stream().map(ProblemaModel::getId).toList(),
                a.getEstrategias().stream().map(EstrategiaAvaliacaoModel::getId).toList());
    }
}