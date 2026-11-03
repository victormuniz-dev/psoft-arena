package br.com.psoft.arena.models.turma;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import br.com.psoft.arena.models.professor.ProfessorModel;
//import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.EstudanteModel;

@Entity
@Table(name = "tb_turma")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String nome;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    @Setter
    private String periodoLetivo;
    
    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    @Setter
    private ProfessorModel professorResponsavel;

    //@OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Getter(AccessLevel.NONE)
    //private List<AtividadeModel> atividades = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "turma_estudante",
        joinColumns = @JoinColumn(name = "turma_id"),
        inverseJoinColumns = @JoinColumn(name = "estudante_id")
    )
    @Getter(AccessLevel.NONE)
    private Set<EstudanteModel> estudantes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTurma status = StatusTurma.ATIVA;

    public void alterarStatus(){
        status.alterarStatus();
    }

   public TurmaModel(String nome, String codigo, String periodoLetivo, ProfessorModel professorResponsavel){
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

    public boolean possuiEstudante(EstudanteModel estudante) {
        return estudantes.contains(estudante);
    }

    //public void adicionarAtividade(AtividadeModel atividade) {
    //    garantirAtiva("receber novas atividades");
    //    atividades.add(atividade);
    //    atividade.setTurma(this);
    //}

    public Set<EstudanteModel> getEstudantes() {
        return Collections.unmodifiableSet(estudantes);
    }

    //public List<AtividadeModel> getAtividades() {
    //    return Collections.unmodifiableList(atividades);
    //}

    private void garantirAtiva(String acao) {
        if (status != StatusTurma.ATIVA) {
            throw new BusinessRuleException("Uma turma encerrada não pode " + acao);
        }
    }
}

