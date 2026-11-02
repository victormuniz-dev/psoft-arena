package br.com.psoft.arena.service.estrategia;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.PromptUpdateDto;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.model.Estrategia.EstrategiaAvaliacao;
import br.com.psoft.arena.model.Estrategia.TipoEstrategia;
import br.com.psoft.arena.repository.Estrategia.EstrategiaAvaliacaoRepository;

@Service 
public class EstrategiaAvaliativaServiceImpl implements EstrategiaAvaliacaoService {

    private  final EstrategiaAvaliacaoRepository repository; 

    public EstrategiaAvaliativaServiceImpl(EstrategiaAvaliacaoRepository repository){
        this.repository = repository; 
    }

    @Override
    @Transactional 
    public EstrategiaAvaliacaoResponseDto cadastrar(EstrategiaAvaliacaoDto dto) {

        EstrategiaAvaliacao estrategia = EstrategiaAvaliacao.builder()
                            .nome(dto.getNome())
                            .tipo(dto.getTipo())
                            .descricao(dto.getDescricao())
                            .promptAvaliacao(dto.getPromptAvaliacao())
                            .promptAvaliacao(dto.getPromptAvaliacao())
                            .build(); 
        return EstrategiaAvaliacaoResponseDto.fromEntity(repository.save(estrategia)); 
        
    }

    @Override
    @Transactional(readOnly = true)
    public EstrategiaAvaliacaoResponseDto BuscaPorId(Long id) {
        EstrategiaAvaliacao estrategia = repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));
        return  EstrategiaAvaliacaoResponseDto.fromEntity(estrategia); 
    }

    @Override
    @Transactional (readOnly = true)
    public List<EstrategiaAvaliacaoResponseDto> liestarTodos() {
       return repository.findAll().stream()
             .map(EstrategiaAvaliacaoResponseDto::fromEntity)
             .toList(); 
    }

    @Override
    @Transactional (readOnly = true)
    public List<EstrategiaAvaliacaoResponseDto> listarPorTipo(TipoEstrategia tipo) {
        return  repository.findByTipo(tipo).stream()
                .map(EstrategiaAvaliacaoResponseDto::fromEntity)
                .toList(); 
    }

    @Override
    @Transactional 
    public EstrategiaAvaliacaoResponseDto atualizar(long id, EstrategiaAvaliacaoDto dto) {
       
        EstrategiaAvaliacao estrategia =  repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));

        estrategia.setNome(dto.getNome());
        estrategia.setTipo(dto.getTipo());
        estrategia.setDescricao(dto.getDescricao());
        estrategia.setPromptAvaliacao(dto.getPromptAvaliacao());

        return  EstrategiaAvaliacaoResponseDto.fromEntity(repository.save(estrategia)); 
    }

    @Override
    @Transactional 
    public EstrategiaAvaliacaoResponseDto atualizarPromt(Long id, PromptUpdateDto dto) {
       EstrategiaAvaliacao estrategia =  repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));
        estrategia.setPromptAvaliacao(dto.getPromptAvaliacao());
        return  EstrategiaAvaliacaoResponseDto.fromEntity(repository.save(estrategia)); 
    }
   

  
}
