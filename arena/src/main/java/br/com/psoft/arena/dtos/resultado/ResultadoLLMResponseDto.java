package br.com.psoft.arena.dtos.resultado;

import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;

public record ResultadoLLMResponseDto(
        String modelo,
        double nota,
        String evidencias,
        String justificativa) {

    public static ResultadoLLMResponseDto de(ResultadoLLMModel resultado) {
        return new ResultadoLLMResponseDto(
                resultado.getLlm().getNomeModelo(), // AJUSTE: getter do nome do modelo
                resultado.getNota(),
                resultado.getEvidencias(),
                resultado.getJustificativa());
    }
}
