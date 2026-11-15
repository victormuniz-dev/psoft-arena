package br.com.psoft.arena.controllers.estudante.consultaEstudante;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.atividade.atividadeEstudante.AtividadeEstudanteResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaEstudante.TurmaEstudanteResponseDto;
import br.com.psoft.arena.services.estudante.consultaestudante.ConsultaAtividadeEstudanteService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controlador REST responsável pelos endpoints de consulta de turmas e atividades 
 * disponíveis para os estudantes (US7)
 */
@RestController 
@RequestMapping("/api/estudante/{estudanteId}")
@RequiredArgsConstructor 
public class ConsultaAtividadeEstudanteController {
    private final ConsultaAtividadeEstudanteService consultaService; 

    /**
     * Endpoint para listar todas as turmas em que o estudante está matriculado. 
     * 
     * @param estudanteId Identificador único do estudante. 
     * @return Resposta HTTP 200 contendo a lista de turmas do estudante. 
     */
    @GetMapping("/turmas")
    public ResponseEntity<List<TurmaEstudanteResponseDto>> listarTurmas(
            @PathVariable  Long estudanteId){
                return ResponseEntity.ok(consultaService.listarTurmas(estudanteId)); 
    }

  /**
     * Endpoint para listar as atividades de uma turma específica em que o estudante participa.
     *
     * @param estudanteId Identificador único do estudante.
     * @param turmaId     Identificador único da turma.
     * @return Resposta HTTP 200 contendo as atividades da turma solicitada.
     */
    @GetMapping("/turma/{turmaId}/atividades")
    public ResponseEntity<List<AtividadeEstudanteResponseDto>> listaratividade(
            @PathVariable  Long estudanteId,
            @PathVariable long turmaId){
                return ResponseEntity.ok(consultaService.listarAtividadesPorTurma(estudanteId, turmaId)); 
    }

    /**
     * Endpoint para listar todas as atividades de todas as turmas do estudante.
     *
     * @param estudanteId Identificador único do estudante.
     * @return Resposta HTTP 200 contendo todas as atividades disponíveis para o estudante.
     */
    @GetMapping("/atividades")
    public ResponseEntity<List<AtividadeEstudanteResponseDto>> listarAtividades(
        @PathVariable  Long estudanteId){
            return ResponseEntity.ok(consultaService.listarAtividades(estudanteId)); 
    }

    /**
     * Endpoint para consultar os detalhes completos de uma atividade específica.
     *
     * @param estudanteId Identificador único do estudante.
     * @param atividadeId Identificador único da atividade.
     * @return Resposta HTTP 200 contendo os detalhes da atividade solicitada.
     */
    @GetMapping ("/atividades/{atividadeId}")
    public ResponseEntity<AtividadeEstudanteResponseDto> buscarAtividade(
            @PathVariable  Long estudanteId,
            @PathVariable  Long atividadeId){
        return  ResponseEntity.ok(consultaService.buscarAtividade(estudanteId, atividadeId)); 
    }

    

    
    
}
