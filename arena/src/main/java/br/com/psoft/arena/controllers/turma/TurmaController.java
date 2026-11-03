package br.com.psoft.arena.controllers.turma;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.estudante.EstudanteDTO;
import br.com.psoft.arena.dtos.turma.TurmaRequestDto;
import br.com.psoft.arena.dtos.turma.TurmaResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaUpdateDto;
import br.com.psoft.arena.services.turma.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar as requisições HTTP relacionadas às Turmas.
 * Fornece endpoints para criação, listagem, atualização, encerramento de turmas
 * e gerenciamento de estudantes matriculados.
 */
@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    /**
     * Cria uma nova turma no sistema.
     * 
     * @param request DTO contendo os dados necessários para a criação da turma.
     * @return Um {@link ResponseEntity} contendo o DTO da turma criada e o link de acesso no cabeçalho Location.
     */
    @PostMapping
    public ResponseEntity<TurmaResponseDto> criar(@Valid @RequestBody TurmaRequestDto request) {
        TurmaResponseDto response = turmaService.criar(request);
        return ResponseEntity.created(URI.create("/turmas/" + response.getId())).body(response);
    }

    /**
     * Lista todas as turmas vinculadas a um professor específico.
     * 
     * @param professorId ID do professor cujas turmas devem ser listadas.
     * @return Uma lista de {@link TurmaResponseDto} representando as turmas do professor.
     */
    @GetMapping
    public List<TurmaResponseDto> listarDoProfessor(@RequestParam Long professorId) {
        return turmaService.listarDoProfessor(professorId);
    }

    /**
     * Busca os detalhes de uma turma específica pelo seu ID.
     * 
     * @param id ID da turma a ser buscada.
     * @return O {@link TurmaResponseDto} correspondente à turma encontrada.
     */
    @GetMapping("/{id}")
    public TurmaResponseDto buscar(@PathVariable Long id) {
        return turmaService.buscarPorId(id);
    }

    /**
     * Edita os dados de uma turma existente.
     * 
     * @param id ID da turma a ser editada.
     * @param request DTO contendo os novos dados para atualização.
     * @return O {@link TurmaResponseDto} atualizado com as modificações.
     */
    @PutMapping("/{id}")
    public TurmaResponseDto editar(@PathVariable Long id,
                                   @Valid @RequestBody TurmaUpdateDto request) {
        return turmaService.editar(id, request);
    }

    /**
     * Encerra uma turma ativa pelo seu ID.
     * 
     * @param id ID da turma a ser encerrada.
     * @return O {@link TurmaResponseDto} representando o estado atualizado da turma.
     */
    @PatchMapping("/{id}/encerrar")
    public TurmaResponseDto encerrar(@PathVariable Long id) {
        return turmaService.encerrar(id);
    }

    /**
     * Lista todos os estudantes matriculados em uma turma específica.
     * 
     * @param id ID da turma.
     * @return Uma lista de {@link EstudanteDTO} com os estudantes matriculados.
     */
    @GetMapping("/{id}/estudantes")
    public List<EstudanteDTO> listarEstudantes(@PathVariable Long id) {
        return turmaService.listarEstudantes(id);
    }

    /**
     * Adiciona um estudante a uma turma através da matrícula.
     * 
     * @param id ID da turma onde o estudante será matriculado.
     * @param matricula Matrícula do estudante a ser adicionado.
     * @return O {@link TurmaResponseDto} atualizado com a inclusão do estudante.
     */
    @PostMapping("/{id}/estudantes/{matricula}")
    public TurmaResponseDto adicionarEstudante(@PathVariable Long id,
                                               @PathVariable String matricula) {
        return turmaService.adicionarEstudante(id, matricula);
    }

    /**
     * Remove um estudante de uma turma através da matrícula.
     * 
     * @param id ID da turma da qual o estudante será removido.
     * @param matricula Matrícula do estudante a ser removido.
     * @return O {@link TurmaResponseDto} atualizado após a remoção do estudante.
     */
    @DeleteMapping("/{id}/estudantes/{matricula}")
    public TurmaResponseDto removerEstudante(@PathVariable Long id,
                                             @PathVariable String matricula) {
        return turmaService.removerEstudante(id, matricula);
    }
}