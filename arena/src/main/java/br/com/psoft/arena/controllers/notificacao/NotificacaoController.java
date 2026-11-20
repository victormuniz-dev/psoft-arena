package br.com.psoft.arena.controllers.notificacao;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.notificacao.NotificacaoResponseDto;
import br.com.psoft.arena.services.notificacao.NotificacaoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping("/estudante/{estudanteId}")
    public List<NotificacaoResponseDto> listarPorEstudante(
        @PathVariable Long estudanteId
    ) {
        return notificacaoService.listarPorEstudante(estudanteId);
    }
}
