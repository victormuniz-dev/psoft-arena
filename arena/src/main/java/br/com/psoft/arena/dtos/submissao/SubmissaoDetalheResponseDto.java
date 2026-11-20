package br.com.psoft.arena.dtos.submissao;

import java.time.LocalDateTime;
import java.util.List;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmissaoDetalheResponseDto {

    private Long id;
    private Long estudanteId;
    private String estudanteNome;
    private String estudanteMatricula;
    private Long atividadeId;
    private LocalDateTime dataHoraSubmissao;
    private Integer versao;
    private StatusSubmissao status;
    private String descricaoStatus;
    private String codigoFonte;
    private List<HistoricoStatusResponseDto> historico;

    public static SubmissaoDetalheResponseDto from(SubmissaoModel submissao) {
        List<HistoricoStatusResponseDto> historico = submissao.getHistorico()
            .stream()
            .map(HistoricoStatusResponseDto::from)
            .toList();

        return new SubmissaoDetalheResponseDto(
            submissao.getId(),
            submissao.getEstudante().getId(),
            submissao.getEstudante().getNome(),
            submissao.getEstudante().getMatricula(),
            submissao.getAtividadeId(),
            submissao.getDataHoraSubmissao(),
            submissao.getVersao(),
            submissao.getStatus(),
            submissao.getStatus().getDescricao(),
            submissao.getCodigoFonte(),
            historico
        );
    }
}
