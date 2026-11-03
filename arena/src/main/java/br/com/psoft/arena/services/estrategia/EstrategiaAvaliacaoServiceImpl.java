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

/**
 * Implementação da regra de negócio para a gestão de estratégias de avaliação.
 * Concentra as validações de domínio, manipulação de persistência e conversões de DTOs.
 */
@Service
@RequiredArgsConstructor
public class EstrategiaAvaliacaoServiceImpl implements EstrategiaAvaliacaoService {

    private  final EstrategiaAvaliacaoRepository repository; 

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException Caso ocorra alguma inconsistência durante a persistência.
     */
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

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException Se nenhuma estratégia for encontrada com o ID fornecido.
     */
    @Override
    @Transactional(readOnly = true)
    public EstrategiaAvaliacaoResponseDto buscarPorId(Long id) {
        EstrategiaAvaliacaoModel estrategia = repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));
        return  EstrategiaAvaliacaoResponseDto.fromEntity(estrategia); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional (readOnly = true)
    public List<EstrategiaAvaliacaoResponseDto> liestarTodos() {
       return repository.findAll().stream()
             .map(EstrategiaAvaliacaoResponseDto::fromEntity)
             .toList(); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional (readOnly = true)
    public List<EstrategiaAvaliacaoResponseDto> listarPorTipo(TipoEstrategia tipo) {
        return  repository.findByTipo(tipo).stream()
                .map(EstrategiaAvaliacaoResponseDto::fromEntity)
                .toList(); 
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException Se a estratégia informada não existir.
     */
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

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException Se o ID fornecido não for localizado.
     */
    @Override
    @Transactional 
    public EstrategiaAvaliacaoResponseDto atualizarPromt(Long id, PromptUpdateDto dto) {
       EstrategiaAvaliacaoModel estrategia =  repository.findById(id)
                            .orElseThrow(() -> new  ResourceNotFoundException("Estratégia de Avaliação", id));
        estrategia.setPromptAvaliacao(dto.getPromptAvaliacao());
        return  EstrategiaAvaliacaoResponseDto.fromEntity(repository.save(estrategia)); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object buscarPorIds(List<Long> copyOf) {
        return repository.findAllById(copyOf);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResourceNotFoundException Se a entidade referente ao ID não for localizada.
     */
    @Override
    public EstrategiaAvaliacaoModel buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estratégia de Avaliação", id));
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<EstrategiaAvaliacaoModel> buscarEntidadesPorIds(List<Long> ids) {
        return repository.findAllById(ids);
    }
   

  
}