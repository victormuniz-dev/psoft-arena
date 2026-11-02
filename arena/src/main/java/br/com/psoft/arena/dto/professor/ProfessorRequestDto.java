package br.com.psoft.arena.dto.professor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfessorRequestDto {
    @NotBlank(message = "O nome do professor não pode ser nulo ou vazio.")
    @Size(max = 255, message = "O nome do professor não pode ter mais de 255 caracteres.")
    private String nome;
}
