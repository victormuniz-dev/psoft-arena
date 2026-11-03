package br.com.psoft.arena.models.estrategia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA que representa uma Estratégia de Avaliação no sistema.
 * Mapeia os dados da tabela 'tb_estrategia_avaliativa' no banco de dados.
 */
@Entity 
@Table (name = "tb_estrategia_avaliativa")
@Data
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class EstrategiaAvaliacaoModel {
    /**
     * Identificador único da estratégia de avaliação no banco de dados (chave primária).
     */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    /**
     * Nome identificador da estratégia de avaliação.
     */
    @Column (nullable = false)
    private  String nome; 

    /**
     * Tipo/categoria da estratégia de avaliação baseada nas regras do enum {@link TipoEstrategia}.
     */
    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private  TipoEstrategia tipo;
    
    /**
     * Descrição detalhada sobre a finalidade e critérios da estratégia de avaliação.
     */
    @Column (columnDefinition = "TEXT", nullable = false)
    private String descricao; 
    
    /**
     * Prompt de comando que será utilizado pela IA para realizar a avaliação automatizada.
     */
    @Column (columnDefinition = "TEXT", nullable = false )
    private  String promptAvaliacao; 
    
}