package br.com.psoft.arena.services.estudante.consultaEstudante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.dtos.estudante.EstudanteRequestDTO;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.services.estudante.EstudanteService;

@ExtendWith (MockitoExtension.class)
public class EstudanteServiceTest {

    @Mock 
    private EstudanteRepository repository; 

    @InjectMocks 
    private EstudanteService service; 

    private EstudanteModel estudanteExistente; 

    @BeforeEach 
    void  setUp(){
        estudanteExistente = new  EstudanteModel(
            "Maria Silva",
            "20231001",
            "Maria@ccc.ufcg.edu.br");
        estudanteExistente.setId(1L);
    }
    @Test
    @DisplayName("Deve criar estudante com sucesso quando matrícula não existe")
    void deveCriarEstudanteComSucesso(){
        EstudanteRequestDTO dto = new EstudanteRequestDTO("João Santos", "20231002", "joao@ccc.ufcg.edu.br"); 
        EstudanteModel salvo = new EstudanteModel("João Santos", "20231002", "joao@ccc.ufcg.edu.br"); 
        salvo.setId(2L);

       when(repository.existsByMatricula("20231002")).thenReturn(false);
       when(repository.save(any(EstudanteModel.class))).thenReturn(salvo);

       EstudanteModel resultado = service.criar(dto); 

       assertNotNull(resultado);
       assertEquals(2L, resultado.getId());
       assertEquals("João Santos", resultado.getNome());
       assertEquals("20231002", resultado.getMatricula());
       verify(repository).save(any(EstudanteModel.class)); 

    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao criar estudante com matrícula duplicada")
    void deveLancarExcecaoAoCriarComMatriculaDuplicada() {
        EstudanteRequestDTO dto = new EstudanteRequestDTO("Outro Nome", "20231001", "outro@ccc.ufcg.edu.br");

        when(repository.existsByMatricula("20231001")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.criar(dto));
        assertTrue(ex.getMessage().contains("20231001"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar estudante por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(estudanteExistente));

        EstudanteModel resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Maria Silva", resultado.getNome());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve editar estudante mantendo a mesma matrícula sem verificar duplicidade")
    void deveEditarEstudanteMantendoMatricula() {
        EstudanteRequestDTO dto = new EstudanteRequestDTO("Maria Atualizada", "20231001", "maria.nova@ccc.ufcg.edu.br");

        when(repository.findById(1L)).thenReturn(Optional.of(estudanteExistente));
        when(repository.save(any(EstudanteModel.class))).thenAnswer(inv -> inv.getArgument(0));

        EstudanteModel resultado = service.editar(1L, dto);

        assertEquals("Maria Atualizada", resultado.getNome());
        assertEquals("maria.nova@ccc.ufcg.edu.br", resultado.getEmail());
        verify(repository, never()).existsByMatricula(anyString());
    }

    @Test
    @DisplayName("Deve editar estudante alterando matrícula quando nova matrícula estiver livre")
    void deveEditarEstudanteAlterandoMatriculaComSucesso() {
        EstudanteRequestDTO dto = new EstudanteRequestDTO("Maria Silva", "20239999", "maria@ccc.ufcg.edu.br");

        when(repository.findById(1L)).thenReturn(Optional.of(estudanteExistente));
        when(repository.existsByMatricula("20239999")).thenReturn(false);
        when(repository.save(any(EstudanteModel.class))).thenAnswer(inv -> inv.getArgument(0));

        EstudanteModel resultado = service.editar(1L, dto);

        assertEquals("20239999", resultado.getMatricula());
        verify(repository).existsByMatricula("20239999");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao editar para matrícula já usada por outro estudante")
    void deveLancarExcecaoAoEditarParaMatriculaDuplicada() {
        EstudanteRequestDTO dto = new EstudanteRequestDTO("Maria Silva", "20238888", "maria@ccc.ufcg.edu.br");

        when(repository.findById(1L)).thenReturn(Optional.of(estudanteExistente));
        when(repository.existsByMatricula("20238888")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.editar(1L, dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os estudantes")
    void deveListarTodosEstudantes() {
        when(repository.findAll()).thenReturn(List.of(estudanteExistente));

        List<EstudanteModel> lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals("Maria Silva", lista.get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar estudante por matrícula com sucesso")
    void deveBuscarPorMatriculaComSucesso() {
        when(repository.findByMatricula("20231001")).thenReturn(Optional.of(estudanteExistente));

        EstudanteModel resultado = service.buscarPorMatricula("20231001");

        assertEquals("20231001", resultado.getMatricula());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar matrícula inexistente")
    void deveLancarExcecaoAoBuscarMatriculaInexistente() {
        when(repository.findByMatricula("00000000")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorMatricula("00000000"));
    }
}
    

