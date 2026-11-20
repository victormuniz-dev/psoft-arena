package br.com.psoft.arena.dtos.turma.TurmaEstudante;

import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta contendo os dados públicos de uma turma sob a perspectiva do estudante.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TurmaEstudanteResponseDto {

    private Long id;
    private String nome;
    private String codigo;
    private String periodoLetivo;
    private String nomeProfessor;
    private StatusTurma status;

    /**
     * Converte uma entidade {@link TurmaModel} em um {@link TurmaEstudanteResponseDto}.
     *
     * @param turma Entidade de domínio da turma.
     * @return Instância preenchida do DTO de resposta.
     */
    public static TurmaEstudanteResponseDto fromEntity(TurmaModel turma) {
        return TurmaEstudanteResponseDto.builder()
                .id(turma.getId())
                .nome(turma.getNome())
                .codigo(turma.getCodigo())
                .periodoLetivo(turma.getPeriodoLetivo())
                .nomeProfessor(turma.getProfessorResponsavel().getNome())
                .status(turma.getStatus())
                .build();
    }
}
