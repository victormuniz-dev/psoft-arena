package br.com.psoft.arena.models.turma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o modelo de dados de uma Turma no sistema[cite: 7].
 * Gerencia informações como nome, código único, período letivo, professor responsável,
 * status, atividades vinculadas e estudantes matriculados[cite: 7].
 */
@Entity
@Table(name = "tb_turma")
@Getter
@NoArgsConstructor
public class TurmaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String nome;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    @Setter
    private String periodoLetivo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    private ProfessorModel professorResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTurma status = StatusTurma.ATIVA;

    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<AtividadeModel> atividades = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "turma_estudante",
        joinColumns = @JoinColumn(name = "turma_id"),
        inverseJoinColumns = @JoinColumn(name = "estudante_id")
    )
    @Getter(AccessLevel.NONE)
    private Set<EstudanteModel> estudantes = new HashSet<>();

    /**
     * Constrói uma nova instância de TurmaModel com os dados básicos necessários[cite: 7].
     * 
     * @param nome Nome da turma[cite: 7].
     * @param codigo Código identificador único da turma[cite: 7].
     * @param periodoLetivo Período letivo correspondente[cite: 7].
     * @param professorResponsavel Professor responsável pela turma[cite: 7].
     */
    public TurmaModel(String nome, String codigo, String periodoLetivo, ProfessorModel professorResponsavel) {
        this.nome = nome;
        this.codigo = codigo;
        this.periodoLetivo = periodoLetivo;
        this.professorResponsavel = professorResponsavel;
    }

    /**
     * Encerra a turma se ela estiver ativa[cite: 7].
     * Lança uma exceção se a turma já estiver encerrada[cite: 7].
     */
    public void encerrar() {
        if (status == StatusTurma.ENCERRADA) {
            throw new BusinessRuleException("A turma já está encerrada");
        }
        this.status = StatusTurma.ENCERRADA;
    }

    /**
     * Adiciona um estudante à turma, garantindo que a turma esteja ativa e que o estudante
     * ainda não esteja associado a ela[cite: 7].
     * 
     * @param estudante O modelo do estudante a ser adicionado[cite: 7].
     */
    public void adicionarEstudante(EstudanteModel estudante) {
        garantirAtiva("receber novos estudantes");
        if (!estudantes.add(estudante)) {
            throw new BusinessRuleException("O estudante já está associado a esta turma");
        }
    }

    /**
     * Remove um estudante da turma[cite: 7].
     * Lança uma exceção caso o estudante não pertença à turma[cite: 7].
     * 
     * @param estudante O modelo do estudante a ser removido[cite: 7].
     */
    public void removerEstudante(EstudanteModel estudante) {
        if (!estudantes.remove(estudante)) {
            throw new BusinessRuleException("O estudante não pertence a esta turma");
        }
    }

    /**
     * Adiciona uma atividade à turma, garantindo que a turma esteja ativa[cite: 7].
     * 
     * @param atividade O modelo da atividade a ser adicionada[cite: 7].
     */
    public void adicionarAtividade(AtividadeModel atividade) {
        garantirAtiva("receber novas atividades");
        atividades.add(atividade);
        atividade.setTurma(this);
    }

    /**
     * Retorna uma visualização em conjunto não modificável dos estudantes matriculados[cite: 7].
     * 
     * @return Conjunto imutável de estudantes[cite: 7].
     */
    public Set<EstudanteModel> getEstudantes() {
        return Collections.unmodifiableSet(estudantes);
    }

    /**
     * Retorna uma lista não modificável das atividades associadas à turma[cite: 7].
     * 
     * @return Lista imutável de atividades[cite: 7].
     */
    public List<AtividadeModel> getAtividades() {
        return Collections.unmodifiableList(atividades);
    }

    /**
     * Método auxiliar privado para validar se a turma está ativa antes de realizar operações restritas[cite: 7].
     * 
     * @param acao A descrição da ação que requer a turma ativa[cite: 7].
     */
    private void garantirAtiva(String acao) {
        if (status != StatusTurma.ATIVA) {
            throw new BusinessRuleException("Uma turma encerrada não pode " + acao);
        }
    }

    /**
     * Define o conjunto de estudantes da turma[cite: 7].
     * 
     * @param estudantes Conjunto de estudantes[cite: 7].
     */
    public void setEstudantes(Set<EstudanteModel> estudantes) {
        this.estudantes = estudantes;
    }

    /**
     * Define a lista de atividades da turma[cite: 7].
     * 
     * @param atividades Lista de atividades[cite: 7].
     */
    public void setAtividades(List<AtividadeModel> atividades) {
        this.atividades = atividades;
    }
}