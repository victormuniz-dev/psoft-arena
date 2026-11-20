package br.com.psoft.arena.models.avaliacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Id;
import br.com.psoft.arena.models.resultado_llm.ResultadoLLMModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoLLMModel> resultados = new ArrayList<>();

     public AvaliacaoModel(SubmissaoModel submissao, NotaFinal notaFinal) {
        this.submissao = submissao;
        this.notaFinal = notaFinal;
        this.dataAvaliacao = LocalDateTime.now();
    }
}

