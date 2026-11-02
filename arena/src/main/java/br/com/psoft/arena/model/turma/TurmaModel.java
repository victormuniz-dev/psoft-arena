package br.com.psoft.arena.model.turma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
<<<<<<< HEAD:arena/src/main/java/br/com/psoft/arena/models/turma/TurmaModel.java
=======
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.model.professor.ProfessorModel;
>>>>>>> 1b553ec (feat(US3): implementa gestão de estratégias de avaliação e exceções customizadas):arena/src/main/java/br/com/psoft/arena/model/turma/TurmaModel.java

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

    public TurmaModel(String nome, String codigo, String periodoLetivo, ProfessorModel professorResponsavel) {
        this.nome = nome;
        this.codigo = codigo;
        this.periodoLetivo = periodoLetivo;
        this.professorResponsavel = professorResponsavel;
    }

    public void encerrar() {
        if (status == StatusTurma.ENCERRADA) {
            throw new BusinessRuleException("A turma já está encerrada");
        }
        this.status = StatusTurma.ENCERRADA;
    }

    public void adicionarEstudante(EstudanteModel estudante) {
        garantirAtiva("receber novos estudantes");
        if (!estudantes.add(estudante)) {
            throw new BusinessRuleException("O estudante já está associado a esta turma");
        }
    }

    public void removerEstudante(EstudanteModel estudante) {
        if (!estudantes.remove(estudante)) {
            throw new BusinessRuleException("O estudante não pertence a esta turma");
        }
    }

    public void adicionarAtividade(AtividadeModel atividade) {
        garantirAtiva("receber novas atividades");
        atividades.add(atividade);
        atividade.setTurma(this);
    }

    public Set<EstudanteModel> getEstudantes() {
        return Collections.unmodifiableSet(estudantes);
    }

    public List<AtividadeModel> getAtividades() {
        return Collections.unmodifiableList(atividades);
    }

    private void garantirAtiva(String acao) {
        if (status != StatusTurma.ATIVA) {
            throw new BusinessRuleException("Uma turma encerrada não pode " + acao);
        }
    }

    public void setEstudantes(Set<EstudanteModel> estudantes) {
        this.estudantes = estudantes;
    }

    public void setAtividades(List<AtividadeModel> atividades) {
        this.atividades = atividades;
    }
}