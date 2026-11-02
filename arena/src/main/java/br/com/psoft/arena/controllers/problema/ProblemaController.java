package br.com.psoft.arena.controllers.problema;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.psoft.arena.dtos.problema.ProblemaRequestDto;
import br.com.psoft.arena.dtos.problema.ProblemaResponseDto;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.services.problema.ProblemaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/problemas")
@RequiredArgsConstructor
public class ProblemaController {

    private final ProblemaService problemaService;

    @PostMapping
    public ResponseEntity<ProblemaResponseDto> criar(
        @Valid @RequestBody ProblemaRequestDto request
    ) {
        ProblemaModel problema = problemaService.criar(
            request.getTitulo(),
            request.getDescricao(),
            request.getRequisitosFuncionais(),
            request.getRequisitosProjeto()
        );

        URI location = URI.create("/problemas/" + problema.getId());

        return ResponseEntity.created(location)
            .body(ProblemaResponseDto.from(problema));
    }

    @GetMapping
    public List<ProblemaResponseDto> listar() {
        return problemaService.listar()
            .stream()
            .map(ProblemaResponseDto::from)
            .toList();
    }

    @GetMapping("/{id}")
    public ProblemaResponseDto buscar(@PathVariable Long id) {
        return ProblemaResponseDto.from(
            problemaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ProblemaResponseDto editar(
        @PathVariable Long id,
        @Valid @RequestBody ProblemaRequestDto request
    ) {
        return ProblemaResponseDto.from(
            problemaService.editar(
                id,
                request.getTitulo(),
                request.getDescricao(),
                request.getRequisitosFuncionais(),
                request.getRequisitosProjeto()
            )
        );
    }

    @PatchMapping("/{id}/desativar")
    public ProblemaResponseDto desativar(@PathVariable Long id) {
        return ProblemaResponseDto.from(
            problemaService.desativar(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        problemaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
