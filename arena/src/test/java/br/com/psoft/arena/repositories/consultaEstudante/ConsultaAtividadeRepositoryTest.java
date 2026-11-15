package br.com.psoft.arena.repositories.consultaEstudante;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.professor.ProfessorRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;

@DataJpaTest 
public class ConsultaAtividadeRepositoryTest {

    @Autowired 
    private AtividadeRepository atividadeRepository; 

    @Autowired 
    private TurmaRepository turmaRepository; 

    @Autowired 
    private EstudanteRepository estudanteRepository; 

    @Autowired 
    private ProfessorRepository professorRepository; 

    private EstudanteModel estudanteMatriculado; 
    private EstudanteModel estudanteNaoMatriculado;
    
    private TurmaModel turmaA; 
    private  TurmaModel turmaB; 

    @BeforeEach 
    void setUp(){
        ProfessorModel professor = new ProfessorModel(); 
        professor.setNome("Professor. Fabio");
        professor = professorRepository.save(professor); 

        estudanteMatriculado = new EstudanteModel("Lucas", "124211807","lucasricardorock@gmai.com");
        estudanteMatriculado = estudanteRepository.save(estudanteMatriculado);

        estudanteNaoMatriculado = new EstudanteModel("Maria", "1242565466","MariaFofinha@gmail.com"); 
        estudanteNaoMatriculado = estudanteRepository.save(estudanteNaoMatriculado);

        turmaA = new TurmaModel("PSoft 2026.1", "124257", "2026.1", professor); 
        turmaA.adicionarEstudante(estudanteMatriculado);
        turmaA = turmaRepository.save(turmaA); 

        turmaB = new TurmaModel("P2 2026.1","242424", "2026.1", professor);
        turmaB = turmaRepository.save(turmaB); 

        AtividadeModel atividadeAberta = new AtividadeModel("Lab 01");
        atividadeAberta.setDescricao("Implementar API REST");
        atividadeAberta.setPrazo(LocalDateTime.now().plusDays(7));
        atividadeAberta.setStatus(StatusAtividade.ABERTA);
        atividadeAberta.setTurma(turmaA);
        atividadeRepository.save(atividadeAberta);

        AtividadeModel atividadeConcluida = new AtividadeModel("Lab 00");
        atividadeConcluida.setDescricao("Setup inicial");
        atividadeConcluida.setPrazo(LocalDateTime.now().minusDays(2));
        atividadeConcluida.setStatus(StatusAtividade.CONCLUIDA);
        atividadeConcluida.setTurma(turmaA);
        atividadeRepository.save(atividadeConcluida);

        AtividadeModel atividadeOutraTurma = new AtividadeModel("Árvores AVL");
        atividadeOutraTurma.setDescricao("Implementar rotação");
        atividadeOutraTurma.setPrazo(LocalDateTime.now().plusDays(5));
        atividadeOutraTurma.setStatus(StatusAtividade.ABERTA);
        atividadeOutraTurma.setTurma(turmaB);
        atividadeRepository.save(atividadeOutraTurma);
    }

    @Test 
    @DisplayName ("Deve verificar corretamente se o estudante pertence á turma")
    void deveVerificarContemEstudanteNaTurma(){
        assertTrue(turmaA.contemEstudante(estudanteMatriculado.getId()));
        assertFalse(turmaA.contemEstudante(estudanteNaoMatriculado.getId()));
        assertFalse(turmaB.contemEstudante(estudanteMatriculado.getId()));
    }

    @Test 
    @DisplayName ("Deve listar todas as turmas em que o estudante está matriculado")
    void deveListarTurmasDoEstudante(){
        List<TurmaModel> turmas = turmaRepository.findAllByEstudanteId(estudanteMatriculado.getId()); 
        List<TurmaModel> turmas2 = turmaRepository.findAllByEstudanteId(estudanteNaoMatriculado.getId()); 

        assertEquals(1, turmas.size());
        assertEquals("PSoft 2026.1", turmas.get(0).getNome());

        assertEquals(0, turmas2.size());
        assertTrue(turmas2.isEmpty()); 

    }
   

    @Test
    @DisplayName("Deve listar todas as atividades (abertas e concluídas) das turmas do estudante")
    void deveListarAtividadesDoEstudanteIncluindoConcluidas() {
        List<AtividadeModel> atividades = atividadeRepository.findAllByEstudanteId(estudanteMatriculado.getId());

        assertEquals(2, atividades.size());
        assertTrue(atividades.stream().anyMatch(a -> a.getStatus() == StatusAtividade.ABERTA));
        assertTrue(atividades.stream().anyMatch(a -> a.getStatus() == StatusAtividade.CONCLUIDA));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para estudante sem turmas matriculadas")
    void deveRetornarVazioParaEstudanteSemTurmas() {
        List<AtividadeModel> atividades = atividadeRepository.findAllByEstudanteId(estudanteNaoMatriculado.getId());

        assertTrue(atividades.isEmpty());
    }
}



    

