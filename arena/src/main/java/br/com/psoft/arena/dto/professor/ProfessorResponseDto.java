package br.com.psoft.arena.dto.professor;

import br.com.psoft.arena.model.professor.ProfessorModel;
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
