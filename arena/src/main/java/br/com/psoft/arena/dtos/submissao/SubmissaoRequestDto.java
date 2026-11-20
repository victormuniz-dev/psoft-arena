package br.com.psoft.arena.dtos.submissao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SubmissaoRequestDto {

    @NotNull(message = "O estudante deve ser informado.")
    @Positive(message = "O identificador do estudante deve ser positivo.")
    private Long estudanteId;

    @NotNull(message = "A atividade deve ser informada.")
    @Positive(message = "O identificador da atividade deve ser positivo.")
    private Long atividadeId;

    @NotBlank(message = "O código-fonte da submissão não pode ser vazio.")
    private String codigoFonte;
}
