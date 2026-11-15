package br.com.psoft.arena.models.atividade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o modelo de dados de uma Atividade no sistema[cite: 9].
 * Gerencia informações como título, descrição, prazo de submissão, status, turma associada,
 * problema, estratégia de avaliação e configurações de LLM vinculadas[cite: 9].
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

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime prazoSubmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtividade status = StatusAtividade.ABERTA;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @Setter
    private TurmaModel turma;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "problema_id", nullable = false)
    private ProblemaModel problema;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estrategia_id", nullable = false)
    private EstrategiaAvaliacaoModel estrategia;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<ConfiguracaoLlmModel> configuracoesLlm = new ArrayList<>();

    /**
     * Constrói uma nova instância de AtividadeModel com os dados básicos informados[cite: 9].
     * 
     * @param titulo O título da atividade[cite: 9].
     * @param descricao A descrição da atividade[cite: 9].
     * @param prazoSubmissao O prazo para submissão da atividade[cite: 9].
     */
    public AtividadeModel(String titulo, String descricao, LocalDateTime prazoSubmissao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prazoSubmissao = prazoSubmissao;
    }

    /**
     * Encerra a atividade se ela estiver aberta.
     * Lança uma exceção se a atividade já estiver encerrada.
     */
    public void encerrar() {
        if (status == StatusAtividade.CONCLUIDA) {
            throw new BusinessRuleException("A atividade já está encerrada");
        }
        this.status = StatusAtividade.CONCLUIDA;
    }

    /**
     * Define o problema da atividade, garantindo que ele esteja ativo[cite: 9].
     * 
     * @param problema O modelo do problema a ser associado[cite: 9].
     */
    public void definirProblema(ProblemaModel problema) {
        if (problema == null) {
            throw new BusinessRuleException("O problema é obrigatório");
        }
        if (!problema.estaAtivo()) {
            throw new BusinessRuleException("Problemas inativos não podem ser associados a atividades");
        }
        this.problema = problema;
    }

    /**
     * Define a estratégia de avaliação da atividade[cite: 9].
     * 
     * @param estrategia O modelo da estratégia de avaliação a ser associada[cite: 9].
     */
    public void definirEstrategia(EstrategiaAvaliacaoModel estrategia) {
        if (estrategia == null) {
            throw new BusinessRuleException("A estratégia de avaliação é obrigatória");
        }
        this.estrategia = estrategia;
    }

    /**
     * Adiciona uma configuração de LLM à atividade, associando o vínculo reverso[cite: 9].
     * 
     * @param configuracao A configuração de LLM a ser adicionada[cite: 9].
     */
    public void adicionarConfiguracaoLlm(ConfiguracaoLlmModel configuracao) {
        if (configuracao == null) {
            throw new BusinessRuleException("A configuração de LLM é obrigatória");
        }
        configuracoesLlm.add(configuracao);
        configuracao.associarAtividade(this);
    }

    /**
     * Retorna uma visualização não modificável das configurações de LLM associadas[cite: 9].
     * 
     * @return Lista imutável de configurações de LLM[cite: 9].
     */
    public List<ConfiguracaoLlmModel> getConfiguracoesLlm() {
        return Collections.unmodifiableList(configuracoesLlm);
    }

    /**
     * Retorna a configuração de LLM marcada como padrão, se existir[cite: 9].
     * 
     * @return A configuração padrão ou {@code null} caso não haja nenhuma[cite: 9].
     */
    public ConfiguracaoLlmModel getConfiguracaoLlmPadrao() {
        return configuracoesLlm.stream()
                .filter(ConfiguracaoLlmModel::isPadrao)
                .findFirst()
                .orElse(null);
    }
}
