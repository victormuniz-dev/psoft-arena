package br.com.psoft.arena.controllers.avaliacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.avaliacao.AvaliacaoResponseDto;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;

@RestController
@RequestMapping("/submissoes")
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoController(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @GetMapping("/{id}/avaliacao")
    public ResponseEntity<AvaliacaoResponseDto> buscar(@PathVariable Long id) {
        return avaliacaoRepository.findBySubmissaoId(id)
                .map(AvaliacaoResponseDto::de)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}