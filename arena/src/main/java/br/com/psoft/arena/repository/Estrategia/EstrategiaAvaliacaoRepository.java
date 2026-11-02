package br.com.psoft.arena.repository.Estrategia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.model.Estrategia.EstrategiaAvaliacao;
import br.com.psoft.arena.model.Estrategia.TipoEstrategia;

public interface EstrategiaAvaliacaoRepository extends  JpaRepository<EstrategiaAvaliacao, Long> {

    List<EstrategiaAvaliacao> findByTipo(TipoEstrategia tipo); 
    
}
