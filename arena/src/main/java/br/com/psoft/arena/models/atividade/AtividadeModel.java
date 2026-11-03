package br.com.psoft.arena.models.atividade;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o modelo de dados de uma Atividade no sistema[cite: 9].
 * Gerencia informações como título, turma associada, problemas e estratégias de avaliação vinculadas[cite: 9].
 */
@Entity
@Table(name = "tb_atividade")
@Getter
@NoArgsConstructor
public class AtividadeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @Setter
    private TurmaModel turma;

    @ManyToMany
    @JoinTable(
        name = "tb_atividade_problema",
        joinColumns = @JoinColumn(name = "atividade_id"),
        inverseJoinColumns = @JoinColumn(name = "problema_id")
    )
    @Getter(AccessLevel.NONE)
    private Set<ProblemaModel> problemas = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "tb_atividade_estrategia",
        joinColumns = @JoinColumn(name = "atividade_id"),
        inverseJoinColumns = @JoinColumn(name = "estrategia_id")
    )
    @Getter(AccessLevel.NONE)
    private Set<EstrategiaAvaliacaoModel> estrategias = new HashSet<>();

    /**
     * Constrói uma nova instância de AtividadeModel com o título especificado[cite: 9].
     * 
     * @param titulo O título da atividade[cite: 9].
     */
    public AtividadeModel(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Adiciona um problema à atividade, garantindo que o problema esteja ativo
     * e que ainda não esteja associado a esta atividade[cite: 9].
     * 
     * @param problema O modelo do problema a ser adicionado[cite: 9].
     */
    public void adicionarProblema(ProblemaModel problema) {
        if (!problema.estaAtivo()) {
            throw new BusinessRuleException("Problemas inativos não podem ser associados a atividades");
        }
        if (!problemas.add(problema)) {
            throw new BusinessRuleException("O problema já está nesta atividade");
        }
    }

    /**
     * Remove um problema da atividade[cite: 9].
     * Lança uma exceção se o problema não pertencer à atividade[cite: 9].
     * 
     * @param problema O modelo do problema a ser removido[cite: 9].
     */
    public void removerProblema(ProblemaModel problema) {
        if (!problemas.remove(problema)) {
            throw new BusinessRuleException("O problema não pertence a esta atividade");
        }
    }

    /**
     * Adiciona uma estratégia de avaliação à atividade[cite: 9].
     * Lança uma exceção caso a estratégia já esteja associada[cite: 9].
     * 
     * @param estrategia O modelo da estratégia de avaliação a ser adicionada[cite: 9].
     */
    public void adicionarEstrategia(EstrategiaAvaliacaoModel estrategia) {
        if (!estrategias.add(estrategia)) {
            throw new BusinessRuleException("A estratégia já está nesta atividade");
        }
    }

    /**
     * Remove uma estratégia de avaliação da atividade[cite: 9].
     * Lança uma exceção se a estratégia não pertencer à atividade[cite: 9].
     * 
     * @param estrategia O modelo da estratégia de avaliação a ser removida[cite: 9].
     */
    public void removerEstrategia(EstrategiaAvaliacaoModel estrategia) {
        if (!estrategias.remove(estrategia)) {
            throw new BusinessRuleException("A estratégia não pertence a esta atividade");
        }
    }

    /**
     * Retorna uma visualização em conjunto não modificável dos problemas associados[cite: 9].
     * 
     * @return Conjunto imutável de problemas[cite: 9].
     */
    public Set<ProblemaModel> getProblemas() {
        return Collections.unmodifiableSet(problemas);
    }

    /**
     * Retorna uma visualização em conjunto não modificável das estratégias de avaliação associadas[cite: 9].
     * 
     * @return Conjunto imutável de estratégias[cite: 9].
     */
    public Set<EstrategiaAvaliacaoModel> getEstrategias() {
        return Collections.unmodifiableSet(estrategias);
    }
}