package br.com.psoft.arena.models.notificacao;

import java.time.LocalDateTime;

import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "tb_notificacao",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_notificacao_submissao",
        columnNames = "submissao_id"
    )
)
@Getter
@NoArgsConstructor
public class NotificacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudante_id", nullable = false)
    private EstudanteModel estudante;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "submissao_id", nullable = false)
    private SubmissaoModel submissao;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public NotificacaoModel(
        EstudanteModel estudante,
        SubmissaoModel submissao,
        String mensagem
    ) {
        if (estudante == null || submissao == null) {
            throw new IllegalArgumentException(
                "Estudante e submissão são obrigatórios para a notificação."
            );
        }

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException(
                "A mensagem da notificação é obrigatória."
            );
        }

        this.estudante = estudante;
        this.submissao = submissao;
        this.mensagem = mensagem;
        this.dataHora = LocalDateTime.now();
    }
}
