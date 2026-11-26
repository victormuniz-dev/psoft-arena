package br.com.psoft.arena.services.avaliacao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.dtos.avaliacao.ResultadoAvaliacaoResponseDto;
import br.com.psoft.arena.client.LLMClient;
import br.com.psoft.arena.client.RespostaLLM;
import br.com.psoft.arena.exception.AvaliacaoFalhaException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AvaliacaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PromptBuilder promptBuilder;
    private final LLMClient llmClient;
    private final CalculadoraNotaFinal calculadora;

    public AvaliacaoService(SubmissaoRepository submissaoRepository,
                            AtividadeRepository atividadeRepository,
                            AvaliacaoRepository avaliacaoRepository,
                            PromptBuilder promptBuilder,
                            LLMClient llmClient,
                            CalculadoraNotaFinal calculadora) {
        this.submissaoRepository = submissaoRepository;
        this.atividadeRepository = atividadeRepository;
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

        Long atividadeId = submissao.getAtividadeId();
        AtividadeModel atividade = atividadeRepository.findById(atividadeId)
                .orElseThrow(() -> new EntityNotFoundException("Atividade não encontrada: " + atividadeId));

        List<ConfiguracaoLlmModel> llms = atividade.getConfiguracoesLlm();
        if (llms == null || llms.isEmpty()) {
            throw new AvaliacaoFalhaException("Atividade sem LLM configurado");
        }

        String prompt = promptBuilder.montar(submissao, atividade);

        // Qualquer exceção aqui sobe: nada é salvo e a transação sofre rollback
        List<ResultadoLLMModel> resultados = new ArrayList<>();
        for (ConfiguracaoLlmModel llm : llms) {
            RespostaLLM resposta = llmClient.avaliar(llm, prompt);
            if (resposta == null || resposta.nota() == null) {
                throw new AvaliacaoFalhaException("Resposta do LLM inválida ou sem nota");
            }
            if (resposta.nota() < 0 || resposta.nota() > 10) {
                throw new AvaliacaoFalhaException("Nota da resposta do LLM fora do intervalo [0, 10]: " + resposta.nota());
            }
            if (resposta.evidencias() == null || resposta.evidencias().isBlank()
                    || resposta.justificativa() == null || resposta.justificativa().isBlank()) {
                throw new AvaliacaoFalhaException("Evidências e justificativa são obrigatórias na resposta do LLM");
            }

            resultados.add(new ResultadoLLMModel(
                    llm,
                    BigDecimal.valueOf(resposta.nota()),
                    resposta.evidencias(),
                    resposta.justificativa()));
        }

        NotaFinal notaFinal = calculadora.calcular(resultados);
        AvaliacaoModel avaliacao = new AvaliacaoModel(submissao, notaFinal, resultados);

        return avaliacaoRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public ResultadoAvaliacaoResponseDto consultarResultado(
        Long estudanteId,
        Long submissaoId
    ) {
        SubmissaoModel submissao = submissaoRepository
            .findByIdAndEstudanteId(submissaoId, estudanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));

        if (submissao.getStatus() != StatusSubmissao.CONCLUIDA) {
            return ResultadoAvaliacaoResponseDto.indisponivel(submissao);
        }

        AvaliacaoModel avaliacao = avaliacaoRepository.findBySubmissaoId(submissaoId)
            .orElseThrow(() -> new BusinessRuleException(
                "A submissão está concluída, mas sua avaliação não foi encontrada."
            ));

        return ResultadoAvaliacaoResponseDto.disponivel(avaliacao);
    }

}
