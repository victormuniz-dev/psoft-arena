package br.com.psoft.arena.dtos.turma;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TurmaRequestDto {

    private Long id;

    @NotBlank(message = "O nome não pode ser vazio.")
    private String nome;

    @NotBlank(message = "O código não pode ser vazio.")
    private String codigo;

    @NotBlank(message = "O período não pode ser vazio.")
    @NotNull
    @Positive
    private Integer periodo;

    @NotNull
    private Long idProfessorResponsavel;
}
