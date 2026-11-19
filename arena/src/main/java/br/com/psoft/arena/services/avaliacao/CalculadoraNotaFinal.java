package br.com.psoft.arena.services.avaliacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.psoft.arena.exception.AvaliacaoFalhaException;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;

@Component
public class CalculadoraNotaFinal {

    public NotaFinal calcular(List<ResultadoLLMModel> resultados) {
    if (resultados == null || resultados.isEmpty()) {
        throw new AvaliacaoFalhaException("Nenhum resultado de LLM para calcular a nota final");
    }

    BigDecimal soma = resultados.stream()
            .map(ResultadoLLMModel::getNota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal media = soma.divide(
            BigDecimal.valueOf(resultados.size()), 1, RoundingMode.HALF_UP);

    String evidencias = combinar(resultados, ResultadoLLMModel::getEvidencias);
    String justificativa = combinar(resultados, ResultadoLLMModel::getJustificativa);

    return new NotaFinal(media, evidencias, justificativa);
}

    private String combinar(List<ResultadoLLMModel> resultados,
                            Function<ResultadoLLMModel, String> campo) {
        return resultados.stream()
                .map(r -> "[" + r.getLlm().getNomeModelo() + "] " + campo.apply(r))
                .collect(Collectors.joining("\n\n"));
    }
}
