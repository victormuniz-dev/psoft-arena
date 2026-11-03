package br.com.psoft.arena.services.turma;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.estudante.EstudanteDTO;
import br.com.psoft.arena.dtos.turma.TurmaRequestDto;
import br.com.psoft.arena.dtos.turma.TurmaResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaUpdateDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.turma.TurmaRepository;
import br.com.psoft.arena.services.estudante.EstudanteService;
import br.com.psoft.arena.services.projessor.ProfessorService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorService professorService;
    private final EstudanteService estudanteService;

    @Transactional
    public TurmaResponseDto criar(TurmaRequestDto request) {
        String codigo = request.getCodigo().trim();
        if (turmaRepository.existsByCodigo(codigo)) {
            throw new BusinessRuleException("Já existe uma turma com o código " + codigo);
        }
        ProfessorModel professor = professorService.buscarPorId(request.getProfessorId());
        TurmaModel turma = new TurmaModel(
                request.getNome().trim(),
                codigo,
                request.getPeriodoLetivo().trim(),
                professor);
        return TurmaResponseDto.de(turmaRepository.save(turma));
    }

    @Transactional(readOnly = true)
    public TurmaResponseDto buscarPorId(Long id) {
        return TurmaResponseDto.de(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<TurmaResponseDto> listarDoProfessor(Long professorId) {
        professorService.buscarPorId(professorId);   // 404 se o professor não existir
        return turmaRepository.findByProfessorResponsavelId(professorId).stream()
                .map(TurmaResponseDto::de)
                .toList();
    }

    @Transactional
    public TurmaResponseDto editar(Long id, TurmaUpdateDto request) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.setNome(request.getNome().trim());
        turma.setPeriodoLetivo(request.getPeriodoLetivo().trim());
        return TurmaResponseDto.de(turma);
    }

    @Transactional
    public TurmaResponseDto encerrar(Long id) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.encerrar();
        return TurmaResponseDto.de(turma);
    }

    @Transactional(readOnly = true)
    public List<EstudanteDTO> listarEstudantes(Long turmaId) {
        return buscarEntidadePorId(turmaId).getEstudantes().stream()
                .map(e -> new EstudanteDTO(e.getNome(), e.getMatricula(), e.getEmail()))
                .toList();
    }

    @Transactional
    public TurmaResponseDto adicionarEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.adicionarEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    @Transactional
    public TurmaResponseDto removerEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.removerEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    @Transactional(readOnly = true)
    public TurmaModel buscarEntidadePorId(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma", id));
    }
}