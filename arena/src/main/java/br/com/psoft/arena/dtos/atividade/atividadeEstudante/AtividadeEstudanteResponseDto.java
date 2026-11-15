package br.com.psoft.arena.dtos.atividade.atividadeEstudante;

import java.time.LocalDateTime;
import java.util.List;

import br.com.psoft.arena.dtos.problema.ProblemaResponseDto;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta contendo os dados de uma atividade visíveis ao estudante (US7).
 * Omite informações internas do professor, como a configuração de LLM e credenciais de API.
 */
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

    /**
     * Converte uma entidade {@link AtividadeModel} em um {@link AtividadeEstudanteResponseDto}.
     * A atividade possui exatamente um problema e uma estratégia de avaliação (US6), que são
     * expostos em listas para preservar o contrato de resposta da US7.
     *
     * @param atividade Entidade de domínio da atividade.
     * @return Instância preenchida do DTO, sem configuração de LLM.
     */
    public static AtividadeEstudanteResponseDto fromEntity(AtividadeModel atividade) {
        List<ProblemaResponseDto> problemaDto = List.of(
                ProblemaResponseDto.from(atividade.getProblema()));

        List<String> estrategiaDto = List.of(atividade.getEstrategia().getNome());

        return AtividadeEstudanteResponseDto.builder()
                .id(atividade.getId())
                .titulo(atividade.getTitulo())
                .codigo(atividade.getTurma().getCodigo())
                .nomeTurma(atividade.getTurma().getNome())
                .descricao(atividade.getDescricao())
                .prazo(atividade.getPrazoSubmissao())
                .status(atividade.getStatus())
                .problemas(problemaDto)
                .estrategiaAvaliativa(estrategiaDto)
                .build();
    }
}
