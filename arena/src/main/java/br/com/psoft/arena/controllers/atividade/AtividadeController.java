package br.com.psoft.arena.controllers.atividade;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.atividade.AtividadeRequestDto;
import br.com.psoft.arena.dtos.atividade.AtividadeResponseDto;
import br.com.psoft.arena.services.atividade.AtividadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsável por gerenciar as requisições HTTP relacionadas às Atividades.
 * Fornece endpoints para criação, listagem, busca e encerramento.
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
    @Operation(summary = "Cria uma nova atividade",
            description = "Cria uma atividade vinculada a uma turma ativa, com um problema ativo, "
                    + "uma estratégia de avaliação e uma configuração de LLM padrão.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Atividade criada com sucesso",
                    content = @Content(schema = @Schema(implementation = AtividadeResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos"),
            @ApiResponse(responseCode = "404", description = "Turma, problema ou estratégia não encontrados"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    @PostMapping("/turmas/{turmaId}/atividades")
    public ResponseEntity<AtividadeResponseDto> criar(@PathVariable Long turmaId,
                                                      @Valid @RequestBody AtividadeRequestDto request) {
        AtividadeResponseDto atividade = atividadeService.criar(
                turmaId,
                request.getTitulo(),
                request.getDescricao(),
                request.getPrazoSubmissao(),
                request.getProblemaId(),
                request.getEstrategiaId(),
                request.getConfiguracaoLlm());

        return ResponseEntity
                .created(URI.create("/atividades/" + atividade.getId()))
                .body(atividade);
    }

    /**
     * Lista todas as atividades vinculadas a uma turma específica.
     * 
     * @param turmaId ID da turma cujas atividades devem ser listadas.
     * @return Uma lista de {@link AtividadeResponseDto} representando as atividades da turma.
     */
    @GetMapping("/turmas/{turmaId}/atividades")
    public List<AtividadeResponseDto> listarDaTurma(@PathVariable Long turmaId) {
        return atividadeService.listarDaTurma(turmaId);
    }

    /**
     * Busca uma atividade específica pelo seu ID.
     * 
     * @param id ID da atividade a ser buscada.
     * @return O {@link AtividadeResponseDto} correspondente à atividade encontrada.
     */
    @GetMapping("/atividades/{id}")
    public AtividadeResponseDto buscar(@PathVariable Long id) {
        return atividadeService.buscarPorId(id);
    }

    /**
     * Encerra uma atividade existente.
     * 
     * @param id ID da atividade a ser encerrada.
     * @return O {@link AtividadeResponseDto} atualizado com status concluído.
     */
    @PatchMapping("/atividades/{id}/encerrar")
    public ResponseEntity<AtividadeResponseDto> encerrar(@PathVariable Long id) {
        return ResponseEntity.ok(atividadeService.encerrar(id));
    }
}
