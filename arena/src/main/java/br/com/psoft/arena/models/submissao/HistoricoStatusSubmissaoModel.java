package br.com.psoft.arena.models.submissao;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_historico_status_submissao")
@Getter
@NoArgsConstructor
public class HistoricoStatusSubmissaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submissao_id", nullable = false)
    private SubmissaoModel submissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao status;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 1000)
    private String observacao;

    public HistoricoStatusSubmissaoModel(
        SubmissaoModel submissao,
        StatusSubmissao status,
        LocalDateTime dataHora,
        String observacao
    ) {
        this.submissao = submissao;
        this.status = status;
        this.dataHora = dataHora;
        this.observacao = observacao;
    }
}
