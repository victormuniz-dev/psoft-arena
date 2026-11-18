package br.com.psoft.arena.dtos.submissao;

import java.time.LocalDateTime;

import br.com.psoft.arena.models.submissao.HistoricoStatusSubmissaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistoricoStatusResponseDto {

    private Long id;
    private StatusSubmissao status;
    private String descricaoStatus;
    private LocalDateTime dataHora;
    private String observacao;

    public static HistoricoStatusResponseDto from(HistoricoStatusSubmissaoModel historico) {
        return new HistoricoStatusResponseDto(
            historico.getId(),
            historico.getStatus(),
            historico.getStatus().getDescricao(),
            historico.getDataHora(),
            historico.getObservacao()
        );
    }
}
