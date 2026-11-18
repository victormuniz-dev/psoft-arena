package br.com.psoft.arena.controllers.estudante;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.estudante.EstudanteRequestDTO;
import br.com.psoft.arena.dtos.estudante.EstudanteResponseDTO;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.services.estudante.EstudanteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/estudantes")
public class EstudanteController {

    private final EstudanteService service;

    public EstudanteController(EstudanteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EstudanteResponseDTO> criar(@Valid @RequestBody EstudanteRequestDTO dto) {
        EstudanteModel criado = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstudanteResponseDTO.from(criado));
    }

    @GetMapping
    public List<EstudanteResponseDTO> listar() {
        return service.listar().stream()
                .map(EstudanteResponseDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EstudanteResponseDTO buscar(@PathVariable Long id) {
        return EstudanteResponseDTO.from(service.buscarPorId(id));
    }

    @GetMapping("/matricula/{matricula}")
    public EstudanteResponseDTO buscarPorMatricula(@PathVariable String matricula) {
        return EstudanteResponseDTO.from(service.buscarPorMatricula(matricula));
    }

    @PutMapping("/{id}")
    public EstudanteResponseDTO editar(@PathVariable Long id,
                                       @Valid @RequestBody EstudanteRequestDTO dto) {
        return EstudanteResponseDTO.from(service.editar(id, dto));
    }
}