package br.com.psoft.arena.dtos.atividade;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtividadeRequestDto {

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotEmpty(message = "Informe pelo menos um problema")
    private List<Long> problemaIds;

    private List<Long> estrategiaIds = new ArrayList<>();
}