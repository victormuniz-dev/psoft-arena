package br.com.psoft.arena.service.Estrategia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dto.estrategiaAvaliativa.PromptUpdateDto;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.model.Estrategia.EstrategiaAvaliacao;
import br.com.psoft.arena.model.Estrategia.TipoEstrategia;
import br.com.psoft.arena.repository.Estrategia.EstrategiaAvaliacaoRepository;
import br.com.psoft.arena.service.estrategia.EstrategiaAvaliativaServiceImpl;

@ExtendWith (MockitoExtension.class)
public class EstrategiaAvaliativaServiceImplTest {

    @Mock 
    private EstrategiaAvaliacaoRepository repository; 

    @InjectMocks 
    private EstrategiaAvaliativaServiceImpl serviceImpl; 

    private  EstrategiaAvaliacaoDto dtoImput; 
    private EstrategiaAvaliacao estrategiaSalva; 

    @BeforeEach
    public void setUp() {
        
        this.dtoImput = new EstrategiaAvaliacaoDto(); 
        dtoImput.setNome("Identificar Duplicação");
        dtoImput.setTipo(TipoEstrategia.CODE_SMELL); 
        dtoImput.setDescricao("Detecta métodos duplicados ou muito extensos.");
        dtoImput.setPromptAvaliacao("Analise o código buscando por duplicação de lógica.");

        estrategiaSalva = EstrategiaAvaliacao.builder()
                .id(1L)
                .nome(dtoImput.getNome())
                .tipo(dtoImput.getTipo())
                .descricao(dtoImput.getDescricao())
                .promptAvaliacao(dtoImput.getPromptAvaliacao())
                .build(); 
    }

    @Test
    @DisplayName("Deve cadastrar estratégia de avaliação com sucesso")
    public  void deveCadastrarEstrategiaComSucesso() {
        when(repository.save(any(EstrategiaAvaliacao.class))).thenReturn(estrategiaSalva);

        EstrategiaAvaliacaoResponseDto response = this.serviceImpl.cadastrar(dtoImput);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Identificar Duplicação", response.getNome());
        assertEquals(TipoEstrategia.CODE_SMELL, response.getTipo());

        verify(repository, times(1)).save(any(EstrategiaAvaliacao.class));
    }
    @Test
    @DisplayName("Deve buscar estratégia por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(estrategiaSalva));

        EstrategiaAvaliacaoResponseDto response = this.serviceImpl.BuscaPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(repository, times(1)).findById(1L);
    }
  
  
    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void deveLancarResourceNotFoundExceptionAoBuscarIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> this.serviceImpl.BuscaPorId(99L)
        );

        assertEquals("Estratégia de Avaliação não encontrado com id 99", ex.getMessage());
        verify(repository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve atualizar o prompt de avaliação com sucesso")
    void deveAtualizarPromptComSucesso() {
      PromptUpdateDto promptDTO = new PromptUpdateDto();
        promptDTO.setPromptAvaliacao("Novo prompt mais rigoroso para Code Smells.");

        when(repository.findById(1L)).thenReturn(Optional.of(estrategiaSalva));
        when(repository.save(any(EstrategiaAvaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstrategiaAvaliacaoResponseDto response = serviceImpl.atualizarPromt(1L, promptDTO);

        assertEquals("Novo prompt mais rigoroso para Code Smells.", response.getPromptAvaliativa());
        verify(repository, times(1)).save(estrategiaSalva);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar prompt de ID inexistente")
    void deveLancarResourceNotFoundExceptionAoAtualizarPromptInexistente() {
        PromptUpdateDto promptDTO = new PromptUpdateDto();
        promptDTO.setPromptAvaliacao("Novo prompt");

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> this.serviceImpl.atualizarPromt(99L, promptDTO)
        );

        verify(repository, never()).save(any());
    }
    
    
}
