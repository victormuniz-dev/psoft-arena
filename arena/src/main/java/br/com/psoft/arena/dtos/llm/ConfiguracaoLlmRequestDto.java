package br.com.psoft.arena.dtos.llm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfiguracaoLlmRequestDto {

    @NotBlank(message = "O nome do modelo é obrigatório")
    @Size(max = 255, message = "O nome do modelo não pode ter mais de 255 caracteres")
    private String nomeModelo;

    @NotBlank(message = "A URL da API é obrigatória")
    @Size(max = 2048, message = "A URL da API não pode ter mais de 2048 caracteres")
    private String urlApi;

    @NotBlank(message = "A chave da API é obrigatória")
    private String chaveApi;
}
