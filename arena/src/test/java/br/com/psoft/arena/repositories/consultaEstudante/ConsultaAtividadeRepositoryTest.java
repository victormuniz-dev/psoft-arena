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
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.estrategia.EstrategiaAvaliacaoRepository;
import br.com.psoft.arena.repositories.problema.ProblemaRepository;
import br.com.psoft.arena.repositories.professor.ProfessorRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;

@DataJpaTest
class ConsultaAtividadeRepositoryTest {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private ProblemaRepository problemaRepository;

    @Autowired
    private EstrategiaAvaliacaoRepository estrategiaRepository;

    private EstudanteModel estudanteMatriculado;
    private EstudanteModel estudanteNaoMatriculado;

    private TurmaModel turmaA;
    private TurmaModel turmaB;

    private ProblemaModel problema;
    private EstrategiaAvaliacaoModel estrategia;

    @BeforeEach
    void setUp() {
        ProfessorModel professor = new ProfessorModel("Professor Fabio");
        professor = professorRepository.save(professor);

        estudanteMatriculado = new EstudanteModel("Lucas", "124211807", "lucasricardorock@gmail.com");
        estudanteMatriculado = estudanteRepository.save(estudanteMatriculado);

        estudanteNaoMatriculado = new EstudanteModel("Maria", "1242565466", "MariaFofinha@gmail.com");
        estudanteNaoMatriculado = estudanteRepository.save(estudanteNaoMatriculado);

        problema = problemaRepository.save(new ProblemaModel(
                "Problema de exemplo",
                "Descrição do problema",
                List.of("RF1"),
                List.of("RP1")));

        estrategia = estrategiaRepository.save(EstrategiaAvaliacaoModel.builder()
                .nome("Avaliação por testes")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Descrição da estratégia")
                .promptAvaliacao("Prompt de avaliação")
                .build());

        turmaA = new TurmaModel("PSoft 2026.1", "124257", "2026.1", professor);
        turmaA.adicionarEstudante(estudanteMatriculado);
        turmaA = turmaRepository.save(turmaA);

        turmaB = new TurmaModel("P2 2026.1", "242424", "2026.1", professor);
        turmaB = turmaRepository.save(turmaB);

        AtividadeModel atividadeAberta = novaAtividade(
                "Lab 01", "Implementar API REST", LocalDateTime.now().plusDays(7), turmaA, false);
        atividadeRepository.save(atividadeAberta);

        AtividadeModel atividadeConcluida = novaAtividade(
                "Lab 00", "Setup inicial", LocalDateTime.now().minusDays(2), turmaA, true);
        atividadeRepository.save(atividadeConcluida);

        AtividadeModel atividadeOutraTurma = novaAtividade(
                "Árvores AVL", "Implementar rotação", LocalDateTime.now().plusDays(5), turmaB, false);
        atividadeRepository.save(atividadeOutraTurma);
    }

    private AtividadeModel novaAtividade(String titulo, String descricao, LocalDateTime prazo,
                                         TurmaModel turma, boolean concluida) {
        AtividadeModel atividade = new AtividadeModel(titulo, descricao, prazo);
        atividade.setTurma(turma);
        atividade.definirProblema(problema);
        atividade.definirEstrategia(estrategia);
        if (concluida) {
            atividade.encerrar();
        }
        return atividade;
    }

    @Test
    @DisplayName("Deve verificar corretamente se o estudante pertence à turma")
    void deveVerificarContemEstudanteNaTurma() {
        assertTrue(turmaA.contemEstudante(estudanteMatriculado.getId()));
        assertFalse(turmaA.contemEstudante(estudanteNaoMatriculado.getId()));
        assertFalse(turmaB.contemEstudante(estudanteMatriculado.getId()));
    }

    @Test
    @DisplayName("Deve tratar ID nulo como estudante não pertencente à turma")
    void deveRetornarFalseParaEstudanteNulo() {
        assertFalse(turmaA.contemEstudante(null));
    }

    @Test
    @DisplayName("Deve listar todas as turmas em que o estudante está matriculado")
    void deveListarTurmasDoEstudante() {
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

    @Test
    @DisplayName("findByTurmaId da US6 continua retornando apenas as atividades da turma informada")
    void deveManterFindByTurmaIdDaUs6() {
        List<AtividadeModel> atividadesTurmaA = atividadeRepository.findByTurmaId(turmaA.getId());
        List<AtividadeModel> atividadesTurmaB = atividadeRepository.findByTurmaId(turmaB.getId());

        assertEquals(2, atividadesTurmaA.size());
        assertEquals(1, atividadesTurmaB.size());
        assertEquals("Árvores AVL", atividadesTurmaB.get(0).getTitulo());
    }
}
