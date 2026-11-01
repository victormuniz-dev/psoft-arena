package br.com.psoft.arena.dtos.turma;

import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TurmaResponseDto {
    private Long id;
    private String nome;
    private String codigo;
    private String periodoLetivo;
    private Long idProfessorResponsavel;
    private String nomeProfessorResponsavel;
    private StatusTurma status;

    public static TurmaResponseDto from(TurmaModel turma) {
        return new TurmaResponseDto(
            turma.getId(),
            turma.getNome(), 
            turma.getCodigo(),
            turma.getPeriodoLetivo(),
            turma.getProfessorResponsavel().getId(),
            turma.getProfessorResponsavel().getNome(),
            turma.getStatus()
        );
    }    
}
