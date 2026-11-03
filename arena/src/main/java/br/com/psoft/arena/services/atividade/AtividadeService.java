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

@Service
@RequiredArgsConstructor
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final TurmaService turmaService;
    private final ProblemaService problemaService;
    private final EstrategiaAvaliacaoService estrategiaService;

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

        turma.adicionarAtividade(atividade);   // falha se a turma estiver encerrada
        return atividadeRepository.save(atividade);
    }

    @Transactional(readOnly = true)
    public AtividadeModel buscarPorId(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<AtividadeModel> listarDaTurma(Long turmaId) {
        turmaService.buscarPorId(turmaId);   // 404 se a turma não existir
        return atividadeRepository.findByTurmaId(turmaId);
    }

    @Transactional
    public AtividadeModel adicionarProblema(Long atividadeId, Long problemaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.adicionarProblema(problemaService.buscarPorId(problemaId));
        return atividade;
    }

    @Transactional
    public AtividadeModel removerProblema(Long atividadeId, Long problemaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.removerProblema(problemaService.buscarPorId(problemaId));
        if (atividade.getProblemas().isEmpty()) {
            throw new BusinessRuleException("A atividade precisa ter pelo menos um problema");
        }
        return atividade;
    }

    @Transactional
    public AtividadeModel adicionarEstrategia(Long atividadeId, Long estrategiaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.adicionarEstrategia(estrategiaService.buscarEntidadePorId(estrategiaId));
        return atividade;
    }

    @Transactional
    public AtividadeModel removerEstrategia(Long atividadeId, Long estrategiaId) {
        AtividadeModel atividade = buscarPorId(atividadeId);
        atividade.removerEstrategia(estrategiaService.buscarEntidadePorId(estrategiaId));
        return atividade;
    }
}