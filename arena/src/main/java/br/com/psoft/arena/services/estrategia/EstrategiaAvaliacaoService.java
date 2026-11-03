package br.com.psoft.arena.services.estrategia;

import java.util.List;

import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dtos.estrategia.PromptUpdateDto;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;

public interface EstrategiaAvaliacaoService{
    
    EstrategiaAvaliacaoResponseDto cadastrar(EstrategiaAvaliacaoDto dto); 

    EstrategiaAvaliacaoResponseDto buscarPorId(Long id); 

    List<EstrategiaAvaliacaoResponseDto> liestarTodos(); 

    List<EstrategiaAvaliacaoResponseDto> listarPorTipo(TipoEstrategia tipo); 

    EstrategiaAvaliacaoResponseDto atualizar(long id, EstrategiaAvaliacaoDto dto);

    EstrategiaAvaliacaoResponseDto atualizarPromt(Long id, PromptUpdateDto dto); 

    EstrategiaAvaliacaoModel buscarEntidadePorId(Long id);

    List<EstrategiaAvaliacaoModel> buscarEntidadesPorIds(List<Long> ids);

    Object buscarPorIds(List<Long> copyOf);
}