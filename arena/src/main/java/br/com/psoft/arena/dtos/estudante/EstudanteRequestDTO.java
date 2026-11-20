package br.com.psoft.arena.dtos.estudante;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EstudanteRequestDTO(
    @NotBlank(message = "O nome é obrigatório") String nome,
    @NotBlank(message = "A matrícula é obrigatória") String matricula,
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido") String email
) {}
