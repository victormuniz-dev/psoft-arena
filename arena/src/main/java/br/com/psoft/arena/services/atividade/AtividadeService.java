package br.com.psoft.arena.services.atividade;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;
import br.com.psoft.arena.services.problema.ProblemaService;
import br.com.psoft.arena.services.turma.TurmaService;
import lombok.RequiredArgsConstructor;

/**
 * Service responsável pela lógica de negócio relacionada às Atividades.
 * Gerencia criação, listagem, busca e manipulação de problemas e estratégias vinculados a atividades.
 */
@Service
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final TurmaService turmaService;
    private final ProblemaService problemaService;
    private final EstrategiaAvaliacaoService estrategiaService;

    /**
     * Cria uma nova atividade associada a uma turma, validando as regras de negócio
     * e os problemas/estratégias informados[cite: 10].
     * 
     * @param turmaId ID da turma à qual a atividade pertencerá[cite: 10].
     * @param titulo Título da atividade[cite: 10].
     * @param problemaIds Lista de IDs dos problemas que compõem a atividade[cite: 10].
     * @param estrategiaIds Lista opcional de IDs das estratégias de avaliação da atividade[cite: 10].
     * @return O {@link AtividadeModel} criado e persistido[cite: 10].
     */
    @Transactional
    public AtividadeModel criar(Long turmaId, String titulo,
                                List<Long> problemaIds, List<Long> estrategiaIds) {
        if (problemaIds == null || problemaIds.isEmpty()) {
            throw new BusinessRuleException("A atividade precisa ter pelo menos um problema");
        }

        TurmaModel turma = turmaService.buscarEntidadePorId(turmaId);
        AtividadeModel atividade = new AtividadeModel(titulo.trim());

        problemaService.buscarPorIds(List.copyOf(new LinkedHashSet<>(problemaIds)))
                .forEach(atividade::adicionarProblema);

        if (estrategiaIds != null && !estrategiaIds.isEmpty()) {
            estrategiaService.buscarEntidadesPorIds(List.copyOf(new LinkedHashSet<>(estrategiaIds)))
        .forEach(atividade::adicionarEstrategia);
        }

        turma.adicionarAtividade(atividade);   // falha se a turma estiver encerrada[cite: 10]
        return atividadeRepository.save(atividade);
    }

    /**
     * Busca uma atividade pelo seu ID[cite: 10].
     * 
     * @param id ID da atividade procurada[cite: 10].
     * @return O {@link AtividadeModel} correspondente[cite: 10].
     * @throws ResourceNotFoundException se a atividade não for encontrada[cite: 10].
     */
    @Transactional(readOnly = true)
    public AtividadeModel buscarPorId(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + id));
    }

    /**
     * Lista todas as atividades vinculadas a uma turma específica[cite: 10].
     * 
     * @param turmaId ID da turma cujas atividades serão listadas[cite: 10].
     * @return Lista de {@link AtividadeModel} da turma[cite: 10].
     */
    @Transactional(readOnly = true)
    public List<AtividadeModel> listarDaTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);   // 404 se a turma não existir[cite: 10]
        return atividadeRepository.findByTurmaId(turmaId);
    }

    /**
     * Adiciona um problema a uma atividade existente[cite: 10].
     * 
     * @param atividadeId ID da atividade[cite: 10].
     * @param problemaId ID do problema a ser adicionado[cite: 10].
     * @return O {@link AtividadeModel} atualizado[cite: 10].
     */
    @Transactional
    public AtividadeModel adicionarProblema(Long atividadeId, Long problemaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.adicionarProblema(problemaService.buscarPorId(problemaId));
        return atividade;
    }

    /**
     * Remove um problema de uma atividade existente, garantindo que ela mantenha pelo menos um problema[cite: 10].
     * 
     * @param atividadeId ID da atividade[cite: 10].
     * @param problemaId ID do problema a ser removido[cite: 10].
     * @return O {@link AtividadeModel} atualizado[cite: 10].
     */
    @Transactional
    public AtividadeModel removerProblema(Long atividadeId, Long problemaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.removerProblema(problemaService.buscarPorId(problemaId));
        if (atividade.getProblemas().isEmpty()) {
            throw new BusinessRuleException("A atividade precisa ter pelo menos um problema");
        }
        return atividade;
    }

    /**
     * Adiciona uma estratégia de avaliação a uma atividade existente[cite: 10].
     * 
     * @param atividadeId ID da atividade[cite: 10].
     * @param estrategiaId ID da estratégia de avaliação a ser adicionada[cite: 10].
     * @return O {@link AtividadeModel} atualizado[cite: 10].
     */
    @Transactional
    public AtividadeModel adicionarEstrategia(Long atividadeId, Long estrategiaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.adicionarEstrategia(estrategiaService.buscarEntidadePorId(estrategiaId));
        return atividade;
    }

    /**
     * Remove uma estratégia de avaliação de uma atividade existente[cite: 10].
     * 
     * @param atividadeId ID da atividade[cite: 10].
     * @param estrategiaId ID da estratégia de avaliação a ser removida[cite: 10].
     * @return O {@link AtividadeModel} atualizado[cite: 10].
     */
    @Transactional
    public AtividadeModel removerEstrategia(Long atividadeId, Long estrategiaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.removerEstrategia(estrategiaService.buscarEntidadePorId(estrategiaId));
        return atividade;
    }
}