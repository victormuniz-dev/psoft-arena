package br.com.psoft.arena.dtos.submissao;

import java.time.LocalDateTime;

import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmissaoResponseDto {

    private Long id;
    private Long estudanteId;
    private String estudanteNome;
    private String estudanteMatricula;
    private Long atividadeId;
    private LocalDateTime dataHoraSubmissao;
    private Integer versao;
    private StatusSubmissao status;
    private String descricaoStatus;

    public static SubmissaoResponseDto from(SubmissaoModel submissao) {
        return new SubmissaoResponseDto(
            submissao.getId(),
            submissao.getEstudante().getId(),
            submissao.getEstudante().getNome(),
            submissao.getEstudante().getMatricula(),
            submissao.getAtividadeId(),
            submissao.getDataHoraSubmissao(),
            submissao.getVersao(),
            submissao.getStatus(),
            submissao.getStatus().getDescricao()
        );
    }
}
