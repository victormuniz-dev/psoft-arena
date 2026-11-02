package br.com.psoft.arena.models.professor;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "tb_professor")
public class ProfessorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "professor_nome", nullable = false)
    @Setter
    private String nome;

    public ProfessorModel(String nome){
        this.nome = nome;
    }
}
