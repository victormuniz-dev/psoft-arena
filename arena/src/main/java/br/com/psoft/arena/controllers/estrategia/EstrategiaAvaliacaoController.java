package br.com.psoft.arena.controllers.estrategia;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoDto;
import br.com.psoft.arena.dtos.estrategia.EstrategiaAvaliacaoResponseDto;
import br.com.psoft.arena.dtos.estrategia.PromptUpdateDto;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.services.estrategia.EstrategiaAvaliacaoService;
import jakarta.validation.Valid;

/**
 * Controller REST responsável por expor os endpoints da API para gestão 
 * das estratégias de avaliação da plataforma PSoft Arena.
 */
@RestController 
@RequestMapping ("/api/estrategia")
public class EstrategiaAvaliacaoController {

    private final EstrategiaAvaliacaoService service; 

    /**
     * Construtor para injeção de dependência do serviço de estratégia de avaliação.
     *
     * @param service Serviço {@link EstrategiaAvaliacaoService} para execução das regras de negócio.
     */
    public EstrategiaAvaliacaoController(EstrategiaAvaliacaoService service){
        this.service = service; 
    }

    /**
     * Endpoint HTTP POST para cadastrar uma nova estratégia de avaliação.
     *
     * @param dto DTO com os dados de criação da estratégia.
     * @return {@link ResponseEntity} contendo o DTO da estratégia cadastrada e status HTTP 201 (Created).
     */
    @PostMapping 
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> cadastrar(@Valid  @RequestBody  EstrategiaAvaliacaoDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto)); 
    }

    /**
     * Endpoint HTTP GET para buscar uma estratégia de avaliação específica pelo seu identificador.
     *
     * @param id Identificador único da estratégia desejada.
     * @return {@link ResponseEntity} contendo o DTO da estratégia encontrada e status HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public  ResponseEntity<EstrategiaAvaliacaoResponseDto> buscarPorId(@PathVariable Long id ){
        return  ResponseEntity.ok(service.buscarPorId(id)); 
    }

    /**
     * Endpoint HTTP GET para listar estratégias de avaliação. 
     * Suporta filtragem opcional por tipo através de parâmetros de consulta (Query Param).
     *
     * @param tipo (Opcional) Tipo da estratégia para filtragem (ex: CODE_SMELL, PADRAO_DE_PROJETO).
     * @return {@link ResponseEntity} com a lista de DTOs das estratégias localizadas e status HTTP 200 (OK).
     */
    @GetMapping 
    public  ResponseEntity<List<EstrategiaAvaliacaoResponseDto>> listarTodas(@RequestParam(required = false) TipoEstrategia tipo){
        ResponseEntity<List<EstrategiaAvaliacaoResponseDto>> out = ResponseEntity.ok(service.liestarTodos()); 
        
        if (tipo != null) {
            out = ResponseEntity.ok(service.listarPorTipo(tipo)); 
            
        }
        return  out; 
    }

    /**
     * Endpoint HTTP PUT para atualizar os dados gerais de uma estratégia de avaliação existente.
     *
     * @param id Identificador da estratégia a ser atualizada.
     * @param dto DTO contendo as novas informações da estratégia.
     * @return {@link ResponseEntity} com o DTO atualizado e status HTTP 200 (OK).
     */

    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> atualizaer(@PathVariable Long id, @Valid @RequestBody EstrategiaAvaliacaoDto dto){

        return  ResponseEntity.ok(service.atualizar(id, dto));
    }

    /**
     * Endpoint HTTP PUT para atualizar especificamente o prompt de instrução de uma estratégia de avaliação.
     *
     * @param id Identificador da estratégia.
     * @param dto DTO contendo o novo texto do prompt.
     * @return {@link ResponseEntity} com o DTO atualizado e status HTTP 200 (OK).
     */
    @PutMapping("/{id}/prompt")
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> atualizarPrompt(
        @PathVariable  Long id,
        @Valid  @RequestBody PromptUpdateDto dto){
            return  ResponseEntity.ok(service.atualizarPromt(id, dto)); 
        } 
    
}