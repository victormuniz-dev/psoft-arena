package br.com.psoft.arena.dtos.turma;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TurmaRequestDto {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O código é obrigatório")
    private String codigo;

    @NotBlank(message = "O período letivo é obrigatório")
    private String periodoLetivo;

    @NotNull(message = "O professor responsável é obrigatório")
    private Long professorId;
}