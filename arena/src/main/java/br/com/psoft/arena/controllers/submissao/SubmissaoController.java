package br.com.psoft.arena.controllers.submissao;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.submissao.HistoricoStatusResponseDto;
import br.com.psoft.arena.dtos.submissao.SubmissaoDetalheResponseDto;
import br.com.psoft.arena.dtos.submissao.SubmissaoRequestDto;
import br.com.psoft.arena.dtos.submissao.SubmissaoResponseDto;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.services.submissao.SubmissaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Submissões", description = "Submissão de soluções e acompanhamento do ciclo de avaliação.")
@RestController
@RequestMapping("/submissoes")
@RequiredArgsConstructor
public class SubmissaoController {

    private final SubmissaoService submissaoService;

    @Operation(summary = "Submeter solução", description = "Registra uma nova versão da solução do estudante e a encaminha automaticamente para avaliação.")
    @ApiResponse(responseCode = "201", description = "Submissão recebida.")
    @ApiResponse(responseCode = "404", description = "Estudante não encontrado.")
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.")
    @PostMapping
    public ResponseEntity<SubmissaoResponseDto> submeter(
        @Valid @RequestBody SubmissaoRequestDto request
    ) {
        SubmissaoModel submissao = submissaoService.registrar(
            request.getEstudanteId(),
            request.getAtividadeId(),
            request.getCodigoFonte()
        );

        submissaoService.encaminharParaProcessamento(submissao.getId());

        URI location = URI.create("/submissoes/" + submissao.getId());

        return ResponseEntity.created(location)
            .body(SubmissaoResponseDto.from(submissao));
    }

    @Operation(summary = "Encaminhar submissão para processamento", description = "Permite apenas que o estudante dono da submissão a encaminhe para a fila de avaliação.")
    @ApiResponse(responseCode = "202", description = "Submissão encaminhada para a fila.")
    @ApiResponse(responseCode = "404", description = "Submissão não encontrada para o estudante informado.")
    @ApiResponse(responseCode = "422", description = "Transição de estado inválida ou fila de processamento cheia.")
    @PostMapping("/{submissaoId}/encaminhar")
    public ResponseEntity<SubmissaoResponseDto> encaminhar(
        @Parameter(description = "Identificador da submissão.") @PathVariable Long submissaoId,
        @Parameter(description = "Identificador do estudante dono da submissão.") @RequestParam Long estudanteId
    ) {
        SubmissaoModel submissao = submissaoService.encaminharParaProcessamento(
            submissaoId,
            estudanteId
        );

        return ResponseEntity.accepted().body(SubmissaoResponseDto.from(submissao));
    }

    @Operation(summary = "Listar submissões do estudante", description = "Retorna apenas as submissões do estudante informado, da mais recente para a mais antiga.")
    @ApiResponse(responseCode = "200", description = "Submissões do estudante.")
    @ApiResponse(responseCode = "404", description = "Estudante não encontrado.")
    @GetMapping("/estudante/{estudanteId}")
    public List<SubmissaoResponseDto> listarPorEstudante(
        @Parameter(description = "Identificador do estudante.") @PathVariable Long estudanteId
    ) {
        return submissaoService.listarPorEstudante(estudanteId)
            .stream()
            .map(SubmissaoResponseDto::from)
            .toList();
    }

    @Operation(summary = "Detalhar submissão do estudante", description = "Retorna o detalhe da submissão, com o estado atual e o histórico de estados em ordem cronológica.")
    @ApiResponse(responseCode = "200", description = "Detalhe da submissão.")
    @ApiResponse(responseCode = "404", description = "Submissão não encontrada para o estudante informado.")
    @GetMapping("/estudante/{estudanteId}/{submissaoId}")
    public SubmissaoDetalheResponseDto detalhar(
        @Parameter(description = "Identificador do estudante.") @PathVariable Long estudanteId,
        @Parameter(description = "Identificador da submissão.") @PathVariable Long submissaoId
    ) {
        return SubmissaoDetalheResponseDto.from(
            submissaoService.buscarDoEstudante(estudanteId, submissaoId)
        );
    }

    @Operation(summary = "Consultar histórico de estados", description = "Retorna o histórico de mudanças de estado da submissão, em ordem cronológica.")
    @ApiResponse(responseCode = "200", description = "Histórico de estados.")
    @ApiResponse(responseCode = "404", description = "Submissão não encontrada para o estudante informado.")
    @GetMapping("/estudante/{estudanteId}/{submissaoId}/historico")
    public List<HistoricoStatusResponseDto> consultarHistorico(
        @Parameter(description = "Identificador do estudante.") @PathVariable Long estudanteId,
        @Parameter(description = "Identificador da submissão.") @PathVariable Long submissaoId
    ) {
        return submissaoService.listarHistorico(estudanteId, submissaoId)
            .stream()
            .map(HistoricoStatusResponseDto::from)
            .toList();
    }
}
