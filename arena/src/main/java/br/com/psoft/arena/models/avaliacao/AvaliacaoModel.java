package br.com.psoft.arena.models.avaliacao;

import java.time.LocalDateTime;

import br.com.psoft.arena.models.submissao.SubmissaoModel;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "avaliacoes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvaliacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "submissao_id", nullable = false, unique = true)
    private SubmissaoModel submissao;

    @Embedded
    private NotaFinal notaFinal;

    @Column(nullable = false)
    private LocalDateTime dataAvaliacao;

     public AvaliacaoModel(SubmissaoModel submissao, NotaFinal notaFinal) {
        this.submissao = submissao;
        this.notaFinal = notaFinal;
        this.dataAvaliacao = LocalDateTime.now();
}
