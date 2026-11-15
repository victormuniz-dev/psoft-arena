package br.com.psoft.arena.services.atividade;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.atividade.AtividadeResponseDto;
import br.com.psoft.arena.dtos.llm.ConfiguracaoLlmRequestDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.services.criptografia.CriptografiaService;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;
import br.com.psoft.arena.services.problema.ProblemaService;
import br.com.psoft.arena.services.turma.TurmaService;
import lombok.RequiredArgsConstructor;

/**
 * Service responsável pela lógica de negócio relacionada às Atividades.
 * Gerencia criação, listagem, busca e encerramento de atividades.
 */
@Service
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final TurmaService turmaService;
    private final ProblemaService problemaService;
    private final EstrategiaAvaliacaoService estrategiaService;
    private final CriptografiaService criptografiaService;

    /**
     * Cria uma nova atividade associada a uma turma, a um problema, a uma estratégia
     * de avaliação e a uma configuração de LLM padrão[cite: 10].
     * 
     * @param turmaId ID da turma à qual a atividade pertencerá[cite: 10].
     * @param titulo Título da atividade[cite: 10].
     * @param descricao Descrição da atividade[cite: 10].
     * @param prazoSubmissao Prazo para submissão da atividade[cite: 10].
     * @param problemaId ID do problema associado à atividade[cite: 10].
     * @param estrategiaId ID da estratégia de avaliação associada à atividade[cite: 10].
     * @param configuracaoLlm Dados da configuração de LLM padrão[cite: 10].
     * @return O {@link AtividadeResponseDto} da atividade criada, sem dados sensíveis[cite: 10].
     */
    @Transactional
    public AtividadeResponseDto criar(Long turmaId, String titulo, String descricao,
                                      LocalDateTime prazoSubmissao, Long problemaId,
                                      Long estrategiaId, ConfiguracaoLlmRequestDto configuracaoLlm) {
        if (turmaId == null) {
            throw new BusinessRuleException("A turma é obrigatória");
        }
        TurmaModel turma = turmaService.buscarEntidadePorId(turmaId);
        if (turma.getStatus() != StatusTurma.ATIVA) {
            throw new BusinessRuleException("Uma turma encerrada não pode receber novas atividades");
        }

        if (problemaId == null) {
            throw new BusinessRuleException("O problema é obrigatório");
        }
        ProblemaModel problema = problemaService.buscarPorId(problemaId);
        if (problema == null || !problema.estaAtivo()) {
            throw new BusinessRuleException("Problemas inativos não podem ser associados a atividades");
        }

        if (estrategiaId == null) {
            throw new BusinessRuleException("A estratégia de avaliação é obrigatória");
        }
        EstrategiaAvaliacaoModel estrategia = estrategiaService.buscarEntidadePorId(estrategiaId);
        if (estrategia == null) {
            throw new BusinessRuleException("A estratégia de avaliação é obrigatória");
        }

        validarDadosBasicos(titulo, descricao, prazoSubmissao);
        validarConfiguracaoLlm(configuracaoLlm);

        AtividadeModel atividade = new AtividadeModel(titulo.trim(), descricao.trim(), prazoSubmissao);
        atividade.definirProblema(problema);
        atividade.definirEstrategia(estrategia);

        ConfiguracaoLlmModel configuracao = new ConfiguracaoLlmModel(
                configuracaoLlm.getNomeModelo().trim(),
                configuracaoLlm.getUrlApi().trim(),
                criptografiaService.criptografar(configuracaoLlm.getChaveApi()),
                true);
        atividade.adicionarConfiguracaoLlm(configuracao);

        turma.adicionarAtividade(atividade);

        AtividadeModel salva = atividadeRepository.save(atividade);
        return AtividadeResponseDto.de(salva);
    }

    /**
     * Busca uma atividade pelo seu ID e retorna o DTO correspondente.
     * A conversão ocorre dentro da transação de leitura, garantindo que os
     * relacionamentos e a coleção de configurações LLM já estejam inicializados.
     * 
     * @param id ID da atividade procurada.
     * @return O {@link AtividadeResponseDto} da atividade encontrada.
     * @throws ResourceNotFoundException se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public AtividadeResponseDto buscarPorId(Long id) {
        return AtividadeResponseDto.de(buscarEntidadePorId(id));
    }

    /**
     * Busca e retorna a entidade {@link AtividadeModel} pura pelo ID.
     * 
     * @param id ID da atividade procurada.
     * @return O modelo {@link AtividadeModel} correspondente.
     * @throws ResourceNotFoundException se a atividade não for encontrada.
     */
    @Transactional(readOnly = true)
    public AtividadeModel buscarEntidadePorId(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + id));
    }

    /**
     * Lista todas as atividades vinculadas a uma turma específica.
     * A conversão para DTO ocorre dentro da transação de leitura, garantindo que os
     * relacionamentos e a coleção de configurações LLM já estejam inicializados.
     * 
     * @param turmaId ID da turma cujas atividades serão listadas.
     * @return Lista de {@link AtividadeResponseDto} da turma.
     */
    @Transactional(readOnly = true)
    public List<AtividadeResponseDto> listarDaTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);   // 404 se a turma não existir
        return atividadeRepository.findByTurmaId(turmaId).stream()
                .map(AtividadeResponseDto::de)
                .toList();
    }

    /**
     * Encerra uma atividade existente, alterando seu status para concluída.
     * 
     * @param id ID da atividade a ser encerrada.
     * @return O {@link AtividadeResponseDto} atualizado.
     */
    @Transactional
    public AtividadeResponseDto encerrar(Long id) {
        AtividadeModel atividade = buscarEntidadePorId(id);
        atividade.encerrar();
        return AtividadeResponseDto.de(atividadeRepository.save(atividade));
    }

    private void validarDadosBasicos(String titulo, String descricao, LocalDateTime prazoSubmissao) {
        if (titulo == null || titulo.isBlank()) {
            throw new BusinessRuleException("O título é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new BusinessRuleException("A descrição é obrigatória");
        }
        if (prazoSubmissao == null) {
            throw new BusinessRuleException("O prazo de submissão é obrigatório");
        }
        if (!prazoSubmissao.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("O prazo de submissão deve ser futuro");
        }
    }

    private void validarConfiguracaoLlm(ConfiguracaoLlmRequestDto configuracaoLlm) {
        if (configuracaoLlm == null) {
            throw new BusinessRuleException("A configuração de LLM é obrigatória");
        }
        if (configuracaoLlm.getNomeModelo() == null || configuracaoLlm.getNomeModelo().isBlank()) {
            throw new BusinessRuleException("O nome do modelo é obrigatório");
        }
        if (configuracaoLlm.getUrlApi() == null || configuracaoLlm.getUrlApi().isBlank()) {
            throw new BusinessRuleException("A URL da API é obrigatória");
        }
        if (configuracaoLlm.getChaveApi() == null || configuracaoLlm.getChaveApi().isBlank()) {
            throw new BusinessRuleException("A chave da API é obrigatória");
        }
    }
}
