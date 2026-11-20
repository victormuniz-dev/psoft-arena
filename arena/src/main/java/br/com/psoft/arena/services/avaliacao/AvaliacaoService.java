package br.com.psoft.arena.services.avaliacao;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import br.com.psoft.arena.client.LLMClient;
import br.com.psoft.arena.client.RespostaLLM;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import br.com.psoft.arena.exception.*;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PromptBuilder promptBuilder;
    private final LLMClient llmClient;
    private final CalculadoraNotaFinal calculadora;

    public AvaliacaoService(SubmissaoRepository submissaoRepository,
                            AvaliacaoRepository avaliacaoRepository,
                            PromptBuilder promptBuilder,
                            LLMClient llmClient,
                            CalculadoraNotaFinal calculadora) {
        this.submissaoRepository = submissaoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.calculadora = calculadora;
    }

    @Transactional
    public AvaliacaoModel avaliar(Long submissaoId) {
        // Uma única avaliação final por submissão
        Optional<AvaliacaoModel> existente = avaliacaoRepository.findBySubmissaoId(submissaoId);
        if (existente.isPresent()) {
            return existente.get();
        }

        SubmissaoModel submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() -> new EntityNotFoundException("Submissão não encontrada: " + submissaoId));

        List<ConfiguracaoLlmModel> llms = submissao.getAtividade().getLlms(); // AJUSTE
        if (llms == null || llms.isEmpty()) {
            throw new AvaliacaoFalhaException("Atividade sem LLM configurado");
        }

        String prompt = promptBuilder.montar(submissao);

        // Qualquer exceção aqui sobe: nada é salvo e a transação sofre rollback
        List<ResultadoLLMModel> resultados = new ArrayList<>();
        for (ConfiguracaoLlmModel llm : llms) {
            RespostaLLM resposta = llmClient.avaliar(llm, prompt);
            resultados.add(new ResultadoLLMModel(
                    llm, resposta.nota(), resposta.evidencias(), resposta.justificativa()));
        }

        NotaFinal notaFinal = calculadora.calcular(resultados);
        AvaliacaoModel avaliacao = new AvaliacaoModel(submissao, notaFinal, resultados);

        return avaliacaoRepository.save(avaliacao);
    }
}
