package br.com.psoft.arena.models.llm;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa a configuração de um LLM associada a uma atividade.
 * A chave da API é armazenada apenas em formato criptografado.
 */
@Entity
@Table(name = "tb_configuracao_llm")
@Getter
@NoArgsConstructor
public class ConfiguracaoLlmModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeModelo;

    @Column(nullable = false, length = 2048)
    private String urlApi;

    @Column(nullable = false, length = 2048)
    private String chaveApiCriptografada;

    @Column(nullable = false)
    private boolean padrao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "atividade_id", nullable = false)
    private AtividadeModel atividade;

    /**
     * Constrói uma configuração de LLM já criptografada.
     *
     * @param nomeModelo Nome do modelo.
     * @param urlApi URL da API do modelo.
     * @param chaveApiCriptografada Chave da API já criptografada.
     * @param padrao Indica se esta é a configuração padrão da atividade.
     */
    public ConfiguracaoLlmModel(String nomeModelo, String urlApi,
                                String chaveApiCriptografada, boolean padrao) {
        this.nomeModelo = nomeModelo;
        this.urlApi = urlApi;
        this.chaveApiCriptografada = chaveApiCriptografada;
        this.padrao = padrao;
    }

    /**
     * Associa a configuração à atividade que a possui.
     *
     * @param atividade Atividade à qual a configuração pertence.
     */
    public void associarAtividade(AtividadeModel atividade) {
        this.atividade = atividade;
    }
}
