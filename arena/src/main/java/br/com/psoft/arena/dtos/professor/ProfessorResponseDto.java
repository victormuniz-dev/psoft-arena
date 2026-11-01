package br.com.psoft.arena.dtos.professor;

import br.com.psoft.arena.models.professor.ProfessorModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfessorResponseDto {
    private Long id;
    private String nome;

    public static ProfessorResponseDto from(ProfessorModel professor) {
        return new ProfessorResponseDto(professor.getId(), professor.getNome());
    }
}
