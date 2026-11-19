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
import br.com.psoft.arena.services.professor.ProfessorService;
import lombok.RequiredArgsConstructor;

/**
 * Service responsável pela lógica de negócio relacionada às Turmas.
 * Gerencia a criação, listagem, edição, encerramento de turmas e a gestão de estudantes matriculados.
 */
@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorService professorService;
    private final EstudanteService estudanteService;

    /**
     * Cria uma nova turma no sistema após validar a unicidade do código e a existência do professor responsável.
     * 
     * @param request DTO contendo os dados para a criação da turma.
     * @return Um {@link TurmaResponseDto} representando a turma criada.
     * @throws BusinessRuleException se já existir uma turma com o código informado.
     */
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

    /**
     * Busca os dados de uma turma pelo seu ID, retornando o DTO correspondente.
     * 
     * @param id ID da turma.
     * @return O {@link TurmaResponseDto} da turma encontrada.
     */
    @Transactional(readOnly = true)
    public TurmaResponseDto buscarPorId(Long id) {
        return TurmaResponseDto.de(buscarEntidadePorId(id));
    }

    /**
     * Lista todas as turmas vinculadas a um professor específico.
     * 
     * @param professorId ID do professor responsável.
     * @return Uma lista de {@link TurmaResponseDto} com as turmas do professor.
     */
    @Transactional(readOnly = true)
    public List<TurmaResponseDto> listarDoProfessor(Long professorId) {
        professorService.buscarPorId(professorId);   // 404 se o professor não existir
        return turmaRepository.findByProfessorResponsavelId(professorId).stream()
                .map(TurmaResponseDto::de)
                .toList();
    }

    /**
     * Edita o nome e o período letivo de uma turma existente.
     * 
     * @param id ID da turma a ser editada.
     * @param request DTO contendo os novos dados.
     * @return O {@link TurmaResponseDto} atualizado.
     */
    @Transactional
    public TurmaResponseDto editar(Long id, TurmaUpdateDto request) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.setNome(request.getNome().trim());
        turma.setPeriodoLetivo(request.getPeriodoLetivo().trim());
        return TurmaResponseDto.de(turma);
    }

    /**
     * Encerra uma turma ativa pelo seu ID.
     * 
     * @param id ID da turma a ser encerrada.
     * @return O {@link TurmaResponseDto} representando a turma encerrada.
     */
    @Transactional
    public TurmaResponseDto encerrar(Long id) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.encerrar();
        return TurmaResponseDto.de(turma);
    }

    /**
     * Lista todos os estudantes matriculados em uma turma específica.
     * 
     * @param turmaId ID da turma.
     * @return Uma lista de {@link EstudanteDTO} correspondente aos estudantes.
     */
    @Transactional(readOnly = true)
    public List<EstudanteDTO> listarEstudantes(Long turmaId) {
        return buscarEntidadePorId(turmaId).getEstudantes().stream()
                .map(e -> new EstudanteDTO(e.getNome(), e.getMatricula(), e.getEmail()))
                .toList();
    }

    /**
     * Adiciona um estudante à turma utilizando a matrícula do estudante.
     * 
     * @param turmaId ID da turma.
     * @param matricula Matrícula do estudante.
     * @return O {@link TurmaResponseDto} atualizado.
     */
    @Transactional
    public TurmaResponseDto adicionarEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.adicionarEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    /**
     * Remove um estudante da turma utilizando a matrícula do estudante.
     * 
     * @param turmaId ID da turma.
     * @param matricula Matrícula do estudante.
     * @return O {@link TurmaResponseDto} atualizado.
     */
    @Transactional
    public TurmaResponseDto removerEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.removerEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    /**
     * Busca e retorna a entidade {@link TurmaModel} pura pelo ID, lançando exceção se não for encontrada.
     * 
     * @param id ID da turma.
     * @return O modelo {@link TurmaModel} correspondente.
     * @throws ResourceNotFoundException se a turma não for encontrada.
     */
    @Transactional(readOnly = true)
    public TurmaModel buscarEntidadePorId(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma", id));
    }
}