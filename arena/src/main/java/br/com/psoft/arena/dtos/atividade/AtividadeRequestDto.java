package br.com.psoft.arena.dtos.atividade;

import java.time.LocalDateTime;

import br.com.psoft.arena.dtos.llm.ConfiguracaoLlmRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "O problema é obrigatório")
    private Long problemaId;

    @NotNull(message = "A estratégia de avaliação é obrigatória")
    private Long estrategiaId;

    @NotNull(message = "A configuração de LLM é obrigatória")
    @Valid
    private ConfiguracaoLlmRequestDto configuracaoLlm;
}
