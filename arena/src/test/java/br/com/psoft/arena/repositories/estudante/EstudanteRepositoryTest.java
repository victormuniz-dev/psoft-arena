package br.com.psoft.arena.repositories.estudante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;

@DataJpaTest 
public class EstudanteRepositoryTest {

    @Autowired 
    private EstudanteRepository repository; 

    private EstudanteModel estudanteSalvo; 

    @BeforeEach 
    void  setUp(){
        repository.deleteAll();; 
        EstudanteModel estudante = new EstudanteModel(
            "Maria Costa",
             "20231001", 
             "maria@ccc.edu.br");
        estudanteSalvo = repository.save(estudante);  
    }

    @Test 
    @DisplayName ("Deve retorna true quando matrícula já existir no banco de dados")
    void deveRetornaTruequandoAMatriculaExistir(){
        boolean existe = repository.existsByMatricula("20231001");
        assertTrue(existe);
    } 

    @Test 
    @DisplayName ("Deve retornar false quando matrícula não existir no banco")
    void deveRetornaTruequandoAMatriculaNaoExistir(){
        boolean naoExiste = repository.existsByMatricula("12423455454"); 
        assertFalse(naoExiste);
    }

    @Test 
    @DisplayName ("Deve buscar estudante por matrícula com sucesso")
    void deveBuscarPorMatriculaComSucesso(){
        Optional<EstudanteModel> encontrado = repository.findByMatricula("20231001");
        assertTrue(encontrado.isPresent());
        assertEquals(estudanteSalvo.getId(), encontrado.get().getId());
        assertEquals("Maria Costa", encontrado.get().getNome());
        assertEquals("maria@ccc.edu.br", encontrado.get().getEmail());
    }

    @Test 
    @DisplayName ("Deve retornar Optional vazio quando matrícula não for encontrada")
    void deveRetornarVazioQuandoMatriculaNaoEncontrada(){
        Optional<EstudanteModel> encontrado = repository.findByMatricula("00000000000"); 
        
        assertTrue(encontrado.isEmpty());
    }

    @Test 
    @DisplayName("Dece salvar, buscar por ID e listar  estudantes corretamente")
    void deveSalvarBuscarEListarEstudante(){
        EstudanteModel novo = new EstudanteModel(
                "João Santos",
                "20231002",
                "joao@ccc.ufcg.edu.br"
        );
        EstudanteModel salvo  = repository.save(novo); 

        assertNotNull(salvo.getId());

        Optional<EstudanteModel> porId = repository.findById(salvo.getId()); 
         assertTrue(porId.isPresent());
         assertEquals("João Santos", porId.get().getNome());

         List<EstudanteModel> todos = repository.findAll(); 
         assertEquals(2, todos.size());
    }
    
    
}
