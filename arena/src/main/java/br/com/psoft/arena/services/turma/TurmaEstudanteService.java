package br.com.psoft.arena.services.turma;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.EstudanteModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.EstudanteRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaEstudanteService {

    private final TurmaRepository turmaRepository;
    private final EstudanteRepository estudanteRepository;

    @Transactional
    public EstudanteModel adicionar(Long turmaId, String matricula) {
        TurmaModel turma = buscarTurma(turmaId);
        EstudanteModel estudante = buscarEstudante(matricula);
        turma.adicionarEstudante(estudante);
        turmaRepository.save(turma);
        return estudante;
    }

    @Transactional
    public void remover(Long turmaId, String matricula) {
        TurmaModel turma = buscarTurma(turmaId);
        EstudanteModel estudante = buscarEstudante(matricula);
        turma.removerEstudante(estudante);
        turmaRepository.save(turma);
    }

    @Transactional(readOnly = true)
    public List<EstudanteModel> listar(Long turmaId) {
        return buscarTurma(turmaId).getEstudantes().stream()
            .sorted(Comparator.comparing(EstudanteModel::getNome, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private TurmaModel buscarTurma(Long turmaId) {
        return turmaRepository.findById(turmaId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma", turmaId));
    }

    private EstudanteModel buscarEstudante(String matricula) {
        return estudanteRepository.findByMatricula(matricula.trim())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Estudante não encontrado com a matrícula " + matricula));
    }
}