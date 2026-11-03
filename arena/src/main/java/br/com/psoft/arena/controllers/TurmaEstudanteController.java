package br.com.psoft.arena.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.psoft.arena.dtos.EstudanteDTO;
import br.com.psoft.arena.dtos.turma.AssociarEstudanteRequestDto;
import br.com.psoft.arena.models.EstudanteModel;
import br.com.psoft.arena.services.turma.TurmaEstudanteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/turmas/{turmaId}/estudantes")
@RequiredArgsConstructor
public class TurmaEstudanteController {

    private final TurmaEstudanteService service;

    @PostMapping
    public ResponseEntity<EstudanteDTO> adicionar(
        @PathVariable Long turmaId,
        @Valid @RequestBody AssociarEstudanteRequestDto request) {
        EstudanteModel estudante = service.adicionar(turmaId, request.getMatricula());
        URI location = URI.create("/turmas/" + turmaId + "/estudantes/" + estudante.getMatricula());
        return ResponseEntity.created(location).body(paraDto(estudante));
    }

    @GetMapping
    public List<EstudanteDTO> listar(@PathVariable Long turmaId) {
        return service.listar(turmaId).stream().map(this::paraDto).toList();
    }

    @DeleteMapping("/{matricula}")
    public ResponseEntity<Void> remover(
        @PathVariable Long turmaId,
        @PathVariable String matricula) {
        service.remover(turmaId, matricula);
        return ResponseEntity.noContent().build();
    }

    private EstudanteDTO paraDto(EstudanteModel e) {
        return new EstudanteDTO(e.getNome(), e.getMatricula(), e.getEmail());
    }
}