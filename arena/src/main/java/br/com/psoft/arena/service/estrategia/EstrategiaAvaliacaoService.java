package br.com.psoft.arena.service.estrategia;

import java.util.List;

import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.PromptUpdateDto;
import br.com.psoft.arena.model.Estrategia.TipoEstrategia;

public interface EstrategiaAvaliacaoService{
    
    EstrategiaAvaliacaoResponseDto cadastrar(EstrategiaAvaliacaoDto dto); 

    EstrategiaAvaliacaoResponseDto BuscaPorId(Long id); 

    List<EstrategiaAvaliacaoResponseDto> liestarTodos(); 

    List<EstrategiaAvaliacaoResponseDto> listarPorTipo(TipoEstrategia tipo); 

    EstrategiaAvaliacaoResponseDto atualizar(long id, EstrategiaAvaliacaoDto dto);

    EstrategiaAvaliacaoResponseDto atualizarPromt(Long id, PromptUpdateDto dto); 
}
