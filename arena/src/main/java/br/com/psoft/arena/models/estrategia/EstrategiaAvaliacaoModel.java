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

@Entity 
@Table (name = "tb_estrategia_avaliativa")
@Data
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class EstrategiaAvaliacaoModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column (nullable = false)
    private  String nome; 

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private  TipoEstrategia tipo;
    
    @Column (columnDefinition = "TEXT", nullable = false)
    private String descricao; 

    @Column (columnDefinition = "TEXT", nullable = false )
    private  String promptAvaliacao; 
    
}