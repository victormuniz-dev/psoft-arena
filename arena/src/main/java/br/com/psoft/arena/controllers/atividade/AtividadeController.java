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

@RestController
@RequiredArgsConstructor
public class AtividadeController {

    private final AtividadeService atividadeService;

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

    @GetMapping("/turmas/{turmaId}/atividades")
    public List<AtividadeResponseDto> listarDaTurma(@PathVariable Long turmaId) {
        return atividadeService.listarDaTurma(turmaId).stream()
                .map(AtividadeResponseDto::de)
                .toList();
    }

    @GetMapping("/atividades/{id}")
    public AtividadeResponseDto buscar(@PathVariable Long id) {
        return AtividadeResponseDto.de(atividadeService.buscarPorId(id));
    }

    @PostMapping("/atividades/{id}/problemas/{problemaId}")
    public AtividadeResponseDto adicionarProblema(@PathVariable Long id,
                                                  @PathVariable Long problemaId) {
        return AtividadeResponseDto.de(atividadeService.adicionarProblema(id, problemaId));
    }

    @DeleteMapping("/atividades/{id}/problemas/{problemaId}")
    public AtividadeResponseDto removerProblema(@PathVariable Long id,
                                                @PathVariable Long problemaId) {
        return AtividadeResponseDto.de(atividadeService.removerProblema(id, problemaId));
    }

    @PostMapping("/atividades/{id}/estrategias/{estrategiaId}")
    public AtividadeResponseDto adicionarEstrategia(@PathVariable Long id,
                                                    @PathVariable Long estrategiaId) {
        return AtividadeResponseDto.de(atividadeService.adicionarEstrategia(id, estrategiaId));
    }

    @DeleteMapping("/atividades/{id}/estrategias/{estrategiaId}")
    public AtividadeResponseDto removerEstrategia(@PathVariable Long id,
                                                  @PathVariable Long estrategiaId) {
        return AtividadeResponseDto.de(atividadeService.removerEstrategia(id, estrategiaId));
    }
}