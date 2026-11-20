package br.com.psoft.arena.dtos.atividade;

import java.time.LocalDateTime;

import br.com.psoft.arena.dtos.llm.ConfiguracaoLlmResponseDto;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AtividadeResponseDto {

    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime prazoSubmissao;
    private StatusAtividade status;
    private Long turmaId;
    private Long problemaId;
    private Long estrategiaId;
    private ConfiguracaoLlmResponseDto configuracaoLlm;

    public static AtividadeResponseDto de(AtividadeModel a) {
        return new AtividadeResponseDto(
                a.getId(),
                a.getTitulo(),
                a.getDescricao(),
                a.getPrazoSubmissao(),
                a.getStatus(),
                a.getTurma().getId(),
                a.getProblema().getId(),
                a.getEstrategia().getId(),
                ConfiguracaoLlmResponseDto.de(a.getConfiguracaoLlmPadrao()));
    }
}
