package br.com.psoft.arena.repositories.estrategia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;


/**
 * Repositório Spring Data JPA para a gestão da persistência de dados da entidade
 * {@link EstrategiaAvaliacaoModel}.
 * Fornece métodos de CRUD padrão e consultas personalizadas por tipo de estratégia.
 */
public interface EstrategiaAvaliacaoRepository extends  JpaRepository<EstrategiaAvaliacaoModel, Long> {


    /**
     * Procura e retorna todas as estratégias de avaliação registadas filtradas pelo seu tipo.
     *
     * @param tipo O tipo de estratégia de avaliação a filtrar (ex: CODE_SMELL, PADRAO_DE_PROJETO).
     * @return Uma lista de {@link EstrategiaAvaliacaoModel} que correspondem ao tipo especificado.
     */
    List<EstrategiaAvaliacaoModel> findByTipo(TipoEstrategia tipo); 
    
}