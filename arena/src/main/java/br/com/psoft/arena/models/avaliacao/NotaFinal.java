package br.com.psoft.arena.models.avaliacao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotaFinal {

    @Column(name = "nota", nullable = false)
    private double nota;

    @Column(name = "evidencia", nullable = false, columnDefinition = "TEXT")
    private String evidencias;

    @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    public NotaFinal(double nota, String evidencias, String justificativa) {
        if (Double.isNaN(nota) || nota < 0 || nota > 10) {
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