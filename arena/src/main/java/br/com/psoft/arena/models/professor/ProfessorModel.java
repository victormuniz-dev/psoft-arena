package br.com.psoft.arena.models.professor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa o modelo de dados de um Professor no sistema[cite: 8].
 * Gerencia informações básicas de identificação e cadastro do docente[cite: 8].
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "tb_professor")
public class ProfessorModel {

    /** Identificador único do professor (Chave primária gerada automaticamente)[cite: 8]. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do professor[cite: 8]. */
    @Column(name = "professor_nome", nullable = false)
    @Setter
    private String nome;

    /**
     * Constrói uma nova instância de ProfessorModel com o nome especificado[cite: 8].
     * 
     * @param nome O nome do professor[cite: 8].
     */
    public ProfessorModel(String nome){
        this.nome = nome;
    }
}