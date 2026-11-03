package br.com.psoft.arena.services.estrategia;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dtos.estrategia.PromptUpdateDto;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.repositories.estrategia.EstrategiaAvaliacaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstrategiaAvaliacaoServiceImpl implements EstrategiaAvaliacaoService {

    private  final EstrategiaAvaliacaoRepository repository; 

    @Override
    @Transactional 
    public EstrategiaAvaliacaoResponseDto cadastrar(EstrategiaAvaliacaoDto dto) {

        EstrategiaAvaliacaoModel estrategia = EstrategiaAvaliacaoModel.builder()
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
    public EstrategiaAvaliacaoResponseDto buscarPorId(Long id) {
        EstrategiaAvaliacaoModel estrategia = repository.findById(id)
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
       
        EstrategiaAvaliacaoModel estrategia =  repository.findById(id)
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
       EstrategiaAvaliacaoModel estrategia =  repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));
        estrategia.setPromptAvaliacao(dto.getPromptAvaliacao());
        return  EstrategiaAvaliacaoResponseDto.fromEntity(repository.save(estrategia)); 
    }

    @Override
    public Object buscarPorIds(List<Long> copyOf) {
        return repository.findAllById(copyOf);
    }

    @Override
    public EstrategiaAvaliacaoModel buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estratégia de Avaliação", id));
    }

    @Override
    public List<EstrategiaAvaliacaoModel> buscarEntidadesPorIds(List<Long> ids) {
        return repository.findAllById(ids);
    }
   

  
}