package br.com.psoft.arena.dtos.turma;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssociarEstudanteRequestDto {
    @NotBlank(message = "A matrícula do estudante não pode ser nula ou vazia.")
    private String matricula;
}