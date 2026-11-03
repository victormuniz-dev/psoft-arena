package br.com.psoft.arena.repositories.turma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;

@DataJpaTest
class TurmaRepositoryTest {

    @Autowired 
    private TurmaRepository turmaRepository;
    
    @Autowired 
    private TestEntityManager  em; // Tipo correto!

    @Test
    @DisplayName("Encontra as turmas de um professor")
    void turmasDoProfessor() {
        ProfessorModel maria = em.persist(new ProfessorModel("Maria"));
        ProfessorModel joao = em.persist(new ProfessorModel("João"));
        em.persist(new TurmaModel("A"));
        em.persist(new TurmaModel("B"));
        em.flush();

        List<TurmaModel> turmas = turmaRepository.findByProfessorResponsavelId(maria.getId());

        assertEquals(1, turmas.size());
        assertEquals("T01", turmas.get(0).getCodigo());
    }
}