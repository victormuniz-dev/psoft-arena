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
    private StatusTurma status;
    private Long professorId;
    private String professorNome;
    private int totalEstudantes;
    private int totalAtividades;

    public static TurmaResponseDto de(TurmaModel turma) {
        return new TurmaResponseDto(
                turma.getId(),
                turma.getNome(),
                turma.getCodigo(),
                turma.getPeriodoLetivo(),
                turma.getStatus(),
                turma.getProfessorResponsavel().getId(),
                turma.getProfessorResponsavel().getNome(),
                turma.getEstudantes().size(),
                turma.getAtividades().size());
    }
}