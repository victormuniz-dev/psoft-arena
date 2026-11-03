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

/**
 * Service responsável pela lógica de negócio relacionada às Turmas[cite: 11].
 * Gerencia a criação, listagem, edição, encerramento de turmas e a gestão de estudantes matriculados[cite: 11].
 */
@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final ProfessorService professorService;
    private final EstudanteService estudanteService;

    /**
     * Cria uma nova turma no sistema após validar a unicidade do código e a existência do professor responsável[cite: 11].
     * 
     * @param request DTO contendo os dados para a criação da turma[cite: 11].
     * @return Um {@link TurmaResponseDto} representando a turma criada[cite: 11].
     * @throws BusinessRuleException se já existir uma turma com o código informado[cite: 11].
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
     * Busca os dados de uma turma pelo seu ID, retornando o DTO correspondente[cite: 11].
     * 
     * @param id ID da turma[cite: 11].
     * @return O {@link TurmaResponseDto} da turma encontrada[cite: 11].
     */
    @Transactional(readOnly = true)
    public TurmaResponseDto buscarPorId(Long id) {
        return TurmaResponseDto.de(buscarEntidadePorId(id));
    }

    /**
     * Lista todas as turmas vinculadas a um professor específico[cite: 11].
     * 
     * @param professorId ID do professor responsável[cite: 11].
     * @return Uma lista de {@link TurmaResponseDto} com as turmas do professor[cite: 11].
     */
    @Transactional(readOnly = true)
    public List<TurmaResponseDto> listarDoProfessor(Long professorId) {
        professorService.buscarPorId(professorId);   // 404 se o professor não existir[cite: 11]
        return turmaRepository.findByProfessorResponsavelId(professorId).stream()
                .map(TurmaResponseDto::de)
                .toList();
    }

    /**
     * Edita o nome e o período letivo de uma turma existente[cite: 11].
     * 
     * @param id ID da turma a ser editada[cite: 11].
     * @param request DTO contendo os novos dados[cite: 11].
     * @return O {@link TurmaResponseDto} atualizado[cite: 11].
     */
    @Transactional
    public TurmaResponseDto editar(Long id, TurmaUpdateDto request) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.setNome(request.getNome().trim());
        turma.setPeriodoLetivo(request.getPeriodoLetivo().trim());
        return TurmaResponseDto.de(turma);
    }

    /**
     * Encerra uma turma ativa pelo seu ID[cite: 11].
     * 
     * @param id ID da turma a ser encerrada[cite: 11].
     * @return O {@link TurmaResponseDto} representando a turma encerrada[cite: 11].
     */
    @Transactional
    public TurmaResponseDto encerrar(Long id) {
        TurmaModel turma = buscarEntidadePorId(id);
        turma.encerrar();
        return TurmaResponseDto.de(turma);
    }

    /**
     * Lista todos os estudantes matriculados em uma turma específica[cite: 11].
     * 
     * @param turmaId ID da turma[cite: 11].
     * @return Uma lista de {@link EstudanteDTO} correspondente aos estudantes[cite: 11].
     */
    @Transactional(readOnly = true)
    public List<EstudanteDTO> listarEstudantes(Long turmaId) {
        return buscarEntidadePorId(turmaId).getEstudantes().stream()
                .map(e -> new EstudanteDTO(e.getNome(), e.getMatricula(), e.getEmail()))
                .toList();
    }

    /**
     * Adiciona um estudante à turma utilizando a matrícula do estudante[cite: 11].
     * 
     * @param turmaId ID da turma[cite: 11].
     * @param matricula Matrícula do estudante[cite: 11].
     * @return O {@link TurmaResponseDto} atualizado[cite: 11].
     */
    @Transactional
    public TurmaResponseDto adicionarEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.adicionarEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    /**
     * Remove um estudante da turma utilizando a matrícula do estudante[cite: 11].
     * 
     * @param turmaId ID da turma[cite: 11].
     * @param matricula Matrícula do estudante[cite: 11].
     * @return O {@link TurmaResponseDto} atualizado[cite: 11].
     */
    @Transactional
    public TurmaResponseDto removerEstudante(Long turmaId, String matricula) {
        TurmaModel turma = buscarEntidadePorId(turmaId);
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        turma.removerEstudante(estudante);
        return TurmaResponseDto.de(turma);
    }

    /**
     * Busca e retorna a entidade {@link TurmaModel} pura pelo ID, lançando exceção se não for encontrada[cite: 11].
     * 
     * @param id ID da turma[cite: 11].
     * @return O modelo {@link TurmaModel} correspondente[cite: 11].
     * @throws ResourceNotFoundException se a turma não for encontrada[cite: 11].
     */
    @Transactional(readOnly = true)
    public TurmaModel buscarEntidadePorId(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma", id));
    }
}