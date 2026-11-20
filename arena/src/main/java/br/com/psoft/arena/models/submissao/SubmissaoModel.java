package br.com.psoft.arena.models.submissao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.estado.EstadoSubmissao;
import br.com.psoft.arena.models.submissao.estado.EstadoSubmissaoFactory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tb_submissao",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_submissao_estudante_atividade_versao",
        columnNames = {"estudante_id", "atividade_id", "versao"}
    )
)
@Getter
@NoArgsConstructor
public class SubmissaoModel {

    private static final int TAMANHO_MAXIMO_OBSERVACAO = 1000;
    private static final String MOTIVO_FALHA_PADRAO =
        "Ocorreu um erro que impediu a conclusão da avaliação.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estudante_id", nullable = false)
    private EstudanteModel estudante;

    @Column(name = "atividade_id", nullable = false)
    private Long atividadeId;

    @Column(name = "data_hora_submissao", nullable = false)
    private LocalDateTime dataHoraSubmissao;

    @Lob
    @Column(name = "codigo_fonte", nullable = false)
    private String codigoFonte;

    @Column(nullable = false)
    private Integer versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao status;

    @OneToMany(mappedBy = "submissao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora ASC")
    private List<HistoricoStatusSubmissaoModel> historico = new ArrayList<>();

    @Transient
    private EstadoSubmissao estadoAtual;

    public SubmissaoModel(
        EstudanteModel estudante,
        Long atividadeId,
        String codigoFonte,
        Integer versao
    ) {
        this.estudante = estudante;
        this.atividadeId = atividadeId;
        this.dataHoraSubmissao = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        this.codigoFonte = codigoFonte;
        this.versao = versao;
        aplicarTransicao(
            StatusSubmissao.SUBMETIDA,
            "Submissão registrada.",
            this.dataHoraSubmissao
        );
    }

    public void encaminharParaFila() {
        estadoAtual().encaminharParaFila(this);
    }

    public void iniciarProcessamento() {
        estadoAtual().iniciarProcessamento(this);
    }

    public void concluir() {
        estadoAtual().concluir(this);
    }

    public void falhar(String motivo) {
        estadoAtual().falhar(this, motivo);
    }

    public void transicionarPara(StatusSubmissao novoStatus, String observacao) {
        aplicarTransicao(
            novoStatus,
            observacao,
            LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
        );
    }

    private void aplicarTransicao(
        StatusSubmissao novoStatus,
        String observacao,
        LocalDateTime dataHora
    ) {
        StatusSubmissao estadoAnterior = this.status;
        EstadoSubmissao novoEstado = EstadoSubmissaoFactory.obter(novoStatus);
        String observacaoNormalizada = normalizarObservacao(novoStatus, observacao);

        this.status = novoStatus;
        this.estadoAtual = novoEstado;
        registrarHistorico(dataHora, observacaoNormalizada);
        notificar(estadoAnterior, novoStatus, dataHora, observacaoNormalizada);
    }

    @PostLoad
    public void restaurarEstado() {
        this.estadoAtual = EstadoSubmissaoFactory.obter(this.status);
    }

    private EstadoSubmissao estadoAtual() {
        if (this.estadoAtual == null) {
            this.estadoAtual = EstadoSubmissaoFactory.obter(this.status);
        }

        return this.estadoAtual;
    }

    private void registrarHistorico(LocalDateTime dataHora, String observacao) {
        this.historico.add(
            new HistoricoStatusSubmissaoModel(
                this,
                this.status,
                dataHora,
                observacao
            )
        );
    }

    private void notificar(
        StatusSubmissao estadoAnterior,
        StatusSubmissao estadoNovo,
        LocalDateTime dataHora,
        String observacao
    ) {
        String matricula = this.estudante == null || this.estudante.getMatricula() == null
            ? "sem matrícula"
            : this.estudante.getMatricula();
        String identificacao = (this.id == null ? "nova" : "#" + this.id)
            + " (versão " + this.versao + ")";
        String descricaoTransicao = estadoAnterior == null
            ? "está no estado " + estadoNovo.getDescricao()
            : "passou do estado " + estadoAnterior.getDescricao()
                + " para o estado " + estadoNovo.getDescricao();
        String motivo = observacao == null || observacao.isBlank()
            ? null
            : observacao;

        String avisoResultado = estadoNovo == StatusSubmissao.CONCLUIDA
            ? " O resultado da avaliação está disponível para consulta."
            : "";

        System.out.println(
            "[PSoft Arena] Notificação para o estudante " + matricula
                + ": submissão " + identificacao
                + " da atividade " + this.atividadeId
                + " " + descricaoTransicao
                + " em " + dataHora + "."
                + (motivo == null ? "" : " (" + motivo + ")")
                + avisoResultado
        );
    }

    private String normalizarObservacao(StatusSubmissao statusAlvo, String observacao) {
        String motivo = observacao;

        if (statusAlvo == StatusSubmissao.FALHA
            && (motivo == null || motivo.isBlank())) {
            motivo = MOTIVO_FALHA_PADRAO;
        }

        if (motivo == null) {
            return null;
        }

        String normalizado = motivo.strip();

        if (normalizado.isEmpty()) {
            return null;
        }

        if (normalizado.length() > TAMANHO_MAXIMO_OBSERVACAO) {
            throw new BusinessRuleException(
                "A observação da mudança de estado da submissão não pode exceder "
                    + TAMANHO_MAXIMO_OBSERVACAO + " caracteres."
            );
        }

        return normalizado;
    }
}
