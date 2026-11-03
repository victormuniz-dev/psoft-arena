package br.com.psoft.arena.repositories.estrategia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;

public interface EstrategiaAvaliacaoRepository extends  JpaRepository<EstrategiaAvaliacaoModel, Long> {

    List<EstrategiaAvaliacaoModel> findByTipo(TipoEstrategia tipo); 
    
}