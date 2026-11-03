package br.com.psoft.arena.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;

class TurmaModelTest {

    private TurmaModel turma;
    private EstudanteModel estudante;

    @BeforeEach
    void setUp() {
        turma = new TurmaModel("PSOFT", "T01", "2026.2", new ProfessorModel("Maria"));
        estudante = new EstudanteModel("Ana", "123", "ana@email.com");
    }

    @Test
    @DisplayName("Turma nasce ativa")
    void nasceAtiva() {
        assertEquals(StatusTurma.ATIVA, turma.getStatus());
    }

    @Test
    @DisplayName("Encerrar duas vezes lança exceção")
    void encerrarDuasVezes() {
        turma.encerrar();
        assertThrows(BusinessRuleException.class, turma::encerrar);
    }

    @Test
    @DisplayName("Turma encerrada não recebe estudante")
    void encerradaNaoRecebeEstudante() {
        turma.encerrar();
        assertThrows(BusinessRuleException.class, () -> turma.adicionarEstudante(estudante));
    }

    @Test
    @DisplayName("Não permite o mesmo estudante duas vezes")
    void estudanteDuplicado() {
        turma.adicionarEstudante(estudante);
        assertThrows(BusinessRuleException.class, () -> turma.adicionarEstudante(estudante));
    }
}
