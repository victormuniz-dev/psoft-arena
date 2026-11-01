package br.com.psoft.arena.controllers.professor;

import java.net.URI;
import java.util.List;

import br.com.psoft.arena.dtos.professor.ProfessorResponseDto;
import br.com.psoft.arena.dtos.professor.ProfessorRequestDto;
import br.com.psoft.arena.models.professor.ProfessorModel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.psoft.arena.services.projessor.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

     private final ProfessorService professorService;

     @PostMapping
     public ResponseEntity<ProfessorResponseDto> criar(
        @Valid @RequestBody ProfessorRequestDto request){
             ProfessorModel professor = professorService.criar(request.getNome());
            URI location = URI.create("/professores/" + professor.getId());
            return ResponseEntity.created(location).body(ProfessorResponseDto.from(professor));
    }

    @GetMapping
    public List<ProfessorResponseDto> listar() {
        return professorService.listar().stream().map(ProfessorResponseDto::from).toList();
    }

    @GetMapping("/{id}")
    public ProfessorResponseDto buscar(@PathVariable Long id){
        return ProfessorResponseDto.from(professorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ProfessorResponseDto editar(
        @PathVariable Long id,
        @Valid @RequestBody ProfessorRequestDto request){
        return ProfessorResponseDto.from(professorService.editar(id, request.getNome()));
    }
}
