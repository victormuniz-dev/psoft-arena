package br.com.psoft.arena.dtos.problema;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProblemaRequestDto {

    @NotBlank(message = "O título não pode ser vazio.")
    @Size(max = 255, message = "O título não pode ter mais de 255 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição não pode ser vazia.")
    @Size(max = 2000, message = "A descrição não pode ter mais de 2000 caracteres.")
    private String descricao;

    @NotEmpty(message = "Informe pelo menos um requisito funcional.")
    private List<@NotBlank(message = "Os requisitos funcionais não podem ser vazios.") String> requisitosFuncionais;

    @NotEmpty(message = "Informe pelo menos um requisito de projeto.")
    private List<@NotBlank(message = "Os requisitos de projeto não podem ser vazios.") String> requisitosProjeto;
}
