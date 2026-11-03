package br.com.psoft.arena.controllers.professor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.professor.ProfessorRequestDto;
import br.com.psoft.arena.dtos.professor.ProfessorResponseDto;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.services.projessor.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar as requisições HTTP relacionadas aos Professores.
 * Fornece endpoints para cadastro, listagem, busca por ID e edição de dados de professores.
 */
@RestController
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorController {

     private final ProfessorService professorService;

     /**
      * Cadastra um novo professor no sistema.
      * 
      * @param request DTO contendo os dados necessários para o cadastro do professor.
      * @return Um {@link ResponseEntity} contendo o DTO do professor criado e o link de acesso no cabeçalho Location.
      */
     @PostMapping
     public ResponseEntity<ProfessorResponseDto> criar(
        @Valid @RequestBody ProfessorRequestDto request){
             ProfessorModel professor = professorService.criar(request.getNome());
            URI location = URI.create("/professores/" + professor.getId());
            return ResponseEntity.created(location).body(ProfessorResponseDto.from(professor));
    }

    /**
     * Lista todos os professores cadastrados no sistema.
     * 
     * @return Uma lista de {@link ProfessorResponseDto} representando todos os professores.
     */
    @GetMapping
    public List<ProfessorResponseDto> listar() {
        return professorService.listar().stream().map(ProfessorResponseDto::from).toList();
    }

    /**
     * Busca os detalhes de um professor específico pelo seu ID.
     * 
     * @param id ID do professor a ser buscado.
     * @return O {@link ProfessorResponseDto} correspondente ao professor encontrado.
     */
    @GetMapping("/{id}")
    public ProfessorResponseDto buscar(@PathVariable Long id){
        return ProfessorResponseDto.from(professorService.buscarPorId(id));
    }

    /**
     * Atualiza os dados de um professor existente.
     * 
     * @param id ID do professor a ser editado.
     * @param request DTO contendo as novas informações para atualização.
     * @return O {@link ProfessorResponseDto} atualizado com as modificações.
     */
    @PutMapping("/{id}")
    public ProfessorResponseDto editar(
        @PathVariable Long id,
        @Valid @RequestBody ProfessorRequestDto request){
        return ProfessorResponseDto.from(professorService.editar(id, request.getNome()));
    }
}