package br.com.psoft.arena.models.resultado_llm;

import java.math.BigDecimal;

import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resultados_llm")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultadoLLMModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private AvaliacaoModel avaliacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "llm_id", nullable = false)
    private ConfiguracaoLlmModel llm;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal nota;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String evidencias;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    public ResultadoLLMModel(ConfiguracaoLlmModel llm, BigDecimal nota,
                             String evidencias, String justificativa) {
        if (llm == null) {
        throw new IllegalArgumentException("O LLM é obrigatório");
    }
    if (nota == null
            || nota.compareTo(BigDecimal.ZERO) < 0
            || nota.compareTo(BigDecimal.TEN) > 0) {
        throw new IllegalArgumentException("A nota deve estar entre 0 e 10");
    }
        this.llm = llm;
        this.nota = nota;
        this.evidencias = evidencias;
        this.justificativa = justificativa;
    }

    // Visibilidade de pacote: só a AvaliacaoModel (mesmo pacote) faz o vínculo
    void vincularAvaliacao(AvaliacaoModel avaliacao) {
        this.avaliacao = avaliacao;
    }
}