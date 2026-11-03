package br.com.psoft.arena.controllers.estudante;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.estudante.EstudanteDTO;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.services.estudante.EstudanteService;

@RestController
@RequestMapping("/api/estudantes")
public class EstudanteController {

    @Autowired
    private EstudanteService estudanteService;

    @PostMapping
    public ResponseEntity<EstudanteModel> criar(@RequestBody EstudanteDTO dto) {
        EstudanteModel novoEstudante = estudanteService.criarEstudante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEstudante);
    }

    @GetMapping
    public ResponseEntity<List<EstudanteModel>> listar() {
        List<EstudanteModel> estudantes = estudanteService.listarEstudantes();
        return ResponseEntity.ok(estudantes);
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<EstudanteModel> consultar(@PathVariable String matricula) {
        EstudanteModel estudante = estudanteService.consultarPorMatricula(matricula);
        return ResponseEntity.ok(estudante);
    }

    @PutMapping("/{matricula}")
    public ResponseEntity<EstudanteModel> atualizar(@PathVariable String matricula, @RequestBody EstudanteDTO dadosAtualizados) {
        EstudanteModel estudanteAtualizado = estudanteService.atualizarEstudante(matricula, dadosAtualizados);
        return ResponseEntity.ok(estudanteAtualizado);
    }
}