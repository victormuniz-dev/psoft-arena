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

@RestController 
@RequestMapping ("/api/estrategia")
public class EstrategiaAvaliacaoController {

    private final EstrategiaAvaliacaoService service; 

    public EstrategiaAvaliacaoController(EstrategiaAvaliacaoService service){
        this.service = service; 
    }

    @PostMapping 
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> cadastrar(@Valid  @RequestBody  EstrategiaAvaliacaoDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(dto)); 
    }

    @GetMapping("/{id}")
    public  ResponseEntity<EstrategiaAvaliacaoResponseDto> buscarPorId(@PathVariable Long id ){
        return  ResponseEntity.ok(service.buscarPorId(id)); 
    }

    @GetMapping 
    public  ResponseEntity<List<EstrategiaAvaliacaoResponseDto>> listarTodas(@RequestParam(required = false) TipoEstrategia tipo){
        ResponseEntity<List<EstrategiaAvaliacaoResponseDto>> out = ResponseEntity.ok(service.liestarTodos()); 
        
        if (tipo != null) {
            out = ResponseEntity.ok(service.listarPorTipo(tipo)); 
            
        }
        return  out; 
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> atualizaer(@PathVariable Long id, @Valid @RequestBody EstrategiaAvaliacaoDto dto){

        return  ResponseEntity.ok(service.atualizar(id, dto));
    }

    @PutMapping("/{id}/prompt")
    public ResponseEntity<EstrategiaAvaliacaoResponseDto> atualizarPrompt(
        @PathVariable  Long id,
        @Valid  @RequestBody PromptUpdateDto dto){
            return  ResponseEntity.ok(service.atualizarPromt(id, dto)); 
        } 
    
}