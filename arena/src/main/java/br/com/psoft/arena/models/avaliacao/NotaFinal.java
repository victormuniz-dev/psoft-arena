package br.com.psoft.arena.models.avaliacao;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotaFinal {

    @Column(name = "nota", nullable = false, precision = 3, scale = 1)
    private BigDecimal nota;
    
    @Column(name="evidencia", nullable = false, columnDefinition = "TEXT")
    private String evidencias;

   @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    public NotaFinal(BigDecimal nota, String evidencias, String justificativa){
        if(nota == null
            || nota.compareTo(BigDecimal.ZERO) < 0
            || nota.compareTo(BigDecimal.TEN) > 0) {
                throw new IllegalArgumentException("A nota deve estar entre 0 e 10");
        }

        if (evidencias == null || evidencias.isBlank()
                || justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Evidências e justificativa são obrigatórias");
        }
        this.nota = nota;
        this.evidencias = evidencias;
        this.justificativa = justificativa;
    }
        
}
