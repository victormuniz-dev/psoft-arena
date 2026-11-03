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

@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    public ResponseEntity<TurmaResponseDto> criar(@Valid @RequestBody TurmaRequestDto request) {
        TurmaResponseDto response = turmaService.criar(request);
        return ResponseEntity.created(URI.create("/turmas/" + response.getId())).body(response);
    }

    @GetMapping
    public List<TurmaResponseDto> listarDoProfessor(@RequestParam Long professorId) {
        return turmaService.listarDoProfessor(professorId);
    }

    @GetMapping("/{id}")
    public TurmaResponseDto buscar(@PathVariable Long id) {
        return turmaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public TurmaResponseDto editar(@PathVariable Long id,
                                   @Valid @RequestBody TurmaUpdateDto request) {
        return turmaService.editar(id, request);
    }

    @PatchMapping("/{id}/encerrar")
    public TurmaResponseDto encerrar(@PathVariable Long id) {
        return turmaService.encerrar(id);
    }

    @GetMapping("/{id}/estudantes")
    public List<EstudanteDTO> listarEstudantes(@PathVariable Long id) {
        return turmaService.listarEstudantes(id);
    }

    @PostMapping("/{id}/estudantes/{matricula}")
    public TurmaResponseDto adicionarEstudante(@PathVariable Long id,
                                               @PathVariable String matricula) {
        return turmaService.adicionarEstudante(id, matricula);
    }

    @DeleteMapping("/{id}/estudantes/{matricula}")
    public TurmaResponseDto removerEstudante(@PathVariable Long id,
                                             @PathVariable String matricula) {
        return turmaService.removerEstudante(id, matricula);
    }
}