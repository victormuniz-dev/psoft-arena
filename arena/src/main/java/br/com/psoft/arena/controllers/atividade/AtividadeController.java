package br.com.psoft.arena.controllers.atividade;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.atividade.AtividadeRequestDto;
import br.com.psoft.arena.dtos.atividade.AtividadeResponseDto;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.services.atividade.AtividadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar as requisições HTTP relacionadas às Atividades.
 * Fornece endpoints para criação, listagem, busca e associação de problemas e estratégias.
 */
@RestController
@RequiredArgsConstructor
public class AtividadeController {

    private final AtividadeService atividadeService;

    /**
     * Cria uma nova atividade associada a uma turma específica.
     * 
     * @param turmaId ID da turma onde a atividade será criada.
     * @param request DTO contendo os dados necessários para a criação da atividade.
     * @return Um {@link ResponseEntity} contendo o DTO da atividade criada e o link de acesso no cabeçalho Location.
     */
    @PostMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<AtividadeResponseDto> criar(@PathVariable Long turmaId,
                                                      @Valid @RequestBody AtividadeRequestDto request) {
        AtividadeModel atividade = atividadeService.criar(
                turmaId,
                request.getTitulo(),
                request.getProblemaIds(),
                request.getEstrategiaIds());

        return ResponseEntity
                .created(URI.create("/atividades/" + atividade.getId()))
                .body(AtividadeResponseDto.de(atividade));
    }

    /**
     * Lista todas as atividades vinculadas a uma turma específica.
     * 
     * @param turmaId ID da turma cujas atividades devem ser listadas.
     * @return Uma lista de {@link AtividadeResponseDto} representando as atividades da turma.
     */
    @GetMapping("/turmas/{turmaId}/atividades")
    public List<AtividadeResponseDto> listarDaTurma(@PathVariable Long turmaId) {
        return atividadeService.listarDaTurma(turmaId).stream()
                .map(AtividadeResponseDto::de)
                .toList();
    }

    /**
     * Busca uma atividade específica pelo seu ID.
     * 
     * @param id ID da atividade a ser buscada.
     * @return O {@link AtividadeResponseDto} correspondente à atividade encontrada.
     */
    @GetMapping("/atividades/{id}")
    public AtividadeResponseDto buscar(@PathVariable Long id) {
        return AtividadeResponseDto.de(atividadeService.buscarPorId(id));
    }

    /**
     * Adiciona um problema a uma atividade existente.
     * 
     * @param id ID da atividade que receberá o problema.
     * @param problemaId ID do problema a ser adicionado.
     * @return O {@link AtividadeResponseDto} atualizado com a alteração.
     */
    @PostMapping("/atividades/{id}/problemas/{problemaId}")
    public AtividadeResponseDto adicionarProblema(@PathVariable Long id,
                                                  @PathVariable Long problemaId) {
        return AtividadeResponseDto.de(atividadeService.adicionarProblema(id, problemaId));
    }

    /**
     * Remove um problema de uma atividade existente.
     * 
     * @param id ID da atividade da qual o problema será removido.
     * @param problemaId ID do problema a ser removido.
     * @return O {@link AtividadeResponseDto} atualizado com a alteração.
     */
    @DeleteMapping("/atividades/{id}/problemas/{problemaId}")
    public AtividadeResponseDto removerProblema(@PathVariable Long id,
                                                @PathVariable Long problemaId) {
        return AtividadeResponseDto.de(atividadeService.removerProblema(id, problemaId));
    }

    /**
     * Adiciona uma estratégia a uma atividade existente.
     * 
     * @param id ID da atividade que receberá a estratégia.
     * @param estrategiaId ID da estratégia a ser adicionada.
     * @return O {@link AtividadeResponseDto} atualizado com a alteração.
     */
    @PostMapping("/atividades/{id}/estrategias/{estrategiaId}")
    public AtividadeResponseDto adicionarEstrategia(@PathVariable Long id,
                                                    @PathVariable Long estrategiaId) {
        return AtividadeResponseDto.de(atividadeService.adicionarEstrategia(id, estrategiaId));
    }

    /**
     * Remove uma estratégia de uma atividade existente.
     * 
     * @param id ID da atividade da qual a estratégia será removida.
     * @param estrategiaId ID da estratégia a ser removida.
     * @return O {@link AtividadeResponseDto} atualizado com a alteração.
     */
    @DeleteMapping("/atividades/{id}/estrategias/{estrategiaId}")
    public AtividadeResponseDto removerEstrategia(@PathVariable Long id,
                                                  @PathVariable Long estrategiaId) {
        return AtividadeResponseDto.de(atividadeService.removerEstrategia(id, estrategiaId));
    }
}