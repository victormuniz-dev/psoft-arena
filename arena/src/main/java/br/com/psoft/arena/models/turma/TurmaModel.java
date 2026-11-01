package br.com.psoft.arena.models.turma;

import br.com.psoft.arena.models.professor.ProfessorModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    @Setter
    private Integer periodo;
    
    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    @Setter
    private ProfessorModel professorResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final StatusTurma status = StatusTurma.ATIVA;

    public void alterarStatus(){
        status.alterarStatus();
    }
}
