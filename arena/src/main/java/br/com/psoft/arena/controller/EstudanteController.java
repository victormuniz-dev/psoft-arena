package br.com.psoft.arena.controller;

import br.com.psoft.arena.dto.EstudanteDTO;
import br.com.psoft.arena.model.Estudante;
import br.com.psoft.arena.service.EstudanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudantes")
public class EstudanteController {

    @Autowired
    private EstudanteService estudanteService;

    @PostMapping
    public ResponseEntity<Estudante> criar(@RequestBody EstudanteDTO dto) {
        Estudante novoEstudante = estudanteService.criarEstudante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEstudante);
    }

    @GetMapping
    public ResponseEntity<List<Estudante>> listar() {
        List<Estudante> estudantes = estudanteService.listarEstudantes();
        return ResponseEntity.ok(estudantes);
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<Estudante> consultar(@PathVariable String matricula) {
        Estudante estudante = estudanteService.consultarPorMatricula(matricula);
        return ResponseEntity.ok(estudante);
    }

    @PutMapping("/{matricula}")
    public ResponseEntity<Estudante> atualizar(@PathVariable String matricula, @RequestBody EstudanteDTO dadosAtualizados) {
        Estudante estudanteAtualizado = estudanteService.atualizarEstudante(matricula, dadosAtualizados);
        return ResponseEntity.ok(estudanteAtualizado);
    }
}