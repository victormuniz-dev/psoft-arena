package br.com.psoft.arena.dtos.atividade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtividadeRequestDto {

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 2000, message = "A descrição não pode ter mais de 2000 caracteres")
    private String descricao;

    @NotNull(message = "O prazo de submissão é obrigatório")
    private LocalDateTime prazoSubmissao;

    @NotEmpty(message = "Informe pelo menos um problema")
    private List<Long> problemaIds;

    private List<Long> estrategiaIds = new ArrayList<>();
}