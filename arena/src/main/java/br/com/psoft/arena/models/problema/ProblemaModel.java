package br.com.psoft.arena.models.problema;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_problema")
@Getter
@NoArgsConstructor
public class ProblemaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @ElementCollection
    @CollectionTable(
        name = "tb_problema_requisitos_funcionais",
        joinColumns = @JoinColumn(name = "problema_id")
    )
    @Column(name = "requisito", nullable = false)
    private List<String> requisitosFuncionais = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "tb_problema_requisitos_projeto",
        joinColumns = @JoinColumn(name = "problema_id")
    )
    @Column(name = "requisito", nullable = false)
    private List<String> requisitosProjeto = new ArrayList<>();

    public ProblemaModel(
        String titulo,
        String descricao,
        List<String> requisitosFuncionais,
        List<String> requisitosProjeto
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = new ArrayList<>(requisitosFuncionais);
        this.requisitosProjeto = new ArrayList<>(requisitosProjeto);
        this.status = StatusProblema.ATIVO;
    }

    public void editar(
        String titulo,
        String descricao,
        List<String> requisitosFuncionais,
        List<String> requisitosProjeto
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = new ArrayList<>(requisitosFuncionais);
        this.requisitosProjeto = new ArrayList<>(requisitosProjeto);
    }

    public void desativar() {
        this.status = StatusProblema.INATIVO;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProblema status = StatusProblema.ATIVO;

}
