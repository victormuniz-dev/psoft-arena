package br.com.psoft.arena.services.avaliacao;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.psoft.arena.exception.*;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;

@Component
public class CalculadoraNotaFinal {

    public NotaFinal calcular(List<ResultadoLLMModel> resultados) {
        if (resultados.isEmpty()) {
            throw new AvaliacaoFalhaException("Nenhum resultado de LLM para calcular a nota final");
        }

        double media = resultados.stream()
                .mapToDouble(ResultadoLLMModel::getNota)
                .average()
                .orElseThrow();
        media = Math.round(media * 100.0) / 100.0;

        String evidencias = combinar(resultados, ResultadoLLMModel::getEvidencias);
        String justificativa = combinar(resultados, ResultadoLLMModel::getJustificativa);

        return new NotaFinal(media, evidencias, justificativa); // AJUSTE ao seu construtor
    }

    private String combinar(List<ResultadoLLMModel> resultados,
                            Function<ResultadoLLMModel, String> campo) {
        return resultados.stream()
                .map(r -> "[" + r.getLlm().getNomeModelo() + "] " + campo.apply(r))
                .collect(Collectors.joining("\n\n"));
    }
}
