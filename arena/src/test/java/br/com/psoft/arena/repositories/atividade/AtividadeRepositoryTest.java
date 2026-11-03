package br.com.psoft.arena.repositories.atividade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;

@DataJpaTest
@DisplayName("AtividadeRepository")
class AtividadeRepositoryTest {

    @Autowired
    private AtividadeRepository repository;

    @Autowired
    private TestEntityManager em;

    private ProfessorModel professor;
    private TurmaModel turmaA;
    private TurmaModel turmaB;
    private TurmaModel turmaSemAtividades;
    private ProblemaModel problemaUsado;
    private ProblemaModel problemaCompartilhado;
    private ProblemaModel problemaLivre;

    @BeforeEach
    void setUp() {
        professor = em.persist(criarProfessor());

        turmaA = em.persist(criarTurma("Turma A", "TURMA-A", professor));
        turmaB = em.persist(criarTurma("Turma B", "TURMA-B", professor));
        turmaSemAtividades = em.persist(criarTurma("Turma C", "TURMA-C", professor));

        problemaUsado = em.persist(criarProblema("Problema 1"));
        problemaCompartilhado = em.persist(criarProblema("Problema 2"));
        problemaLivre = em.persist(criarProblema("Problema 3"));

        persistirAtividade("Lista 1", turmaA, problemaUsado, problemaCompartilhado);
        persistirAtividade("Lista 2", turmaA);
        persistirAtividade("Lista 3", turmaB, problemaCompartilhado);

        em.flush();
        em.clear();
    }

    private ProfessorModel criarProfessor() {
        return new ProfessorModel("Professor Teste");
    }

    private TurmaModel criarTurma(String nome, String codigo, ProfessorModel professor) {
        TurmaModel turma = new TurmaModel(nome);
        turma.setPeriodoLetivo("2026.1");
        ReflectionTestUtils.setField(turma, "codigo", codigo);
        ReflectionTestUtils.setField(turma, "professorResponsavel", professor);
        return turma;
    }

    private ProblemaModel criarProblema(String titulo) {
        return new ProblemaModel(titulo, "Descrição de " + titulo, List.of("RF1"), List.of("RP1"));
    }

    private AtividadeModel persistirAtividade(String titulo, TurmaModel turma, ProblemaModel... problemas) {
        AtividadeModel atividade = new AtividadeModel(titulo);
        atividade.setTurma(turma);
        for (ProblemaModel problema : problemas) {
            atividade.adicionarProblema(problema);
        }
        return em.persist(atividade);
    }

    @Test
    @DisplayName("findByTurmaId retorna apenas as atividades da turma informada")
    void findByTurmaIdRetornaApenasDaTurma() {
        List<AtividadeModel> resultado = repository.findByTurmaId(turmaA.getId());

        assertThat(resultado)
                .extracting(AtividadeModel::getTitulo)
                .containsExactlyInAnyOrder("Lista 1", "Lista 2");
    }

    @Test
    @DisplayName("findByTurmaId retorna a única atividade de outra turma")
    void findByTurmaIdRetornaAtividadeDeOutraTurma() {
        List<AtividadeModel> resultado = repository.findByTurmaId(turmaB.getId());

        assertThat(resultado)
                .extracting(AtividadeModel::getTitulo)
                .containsExactly("Lista 3");
    }

    @Test
    @DisplayName("findByTurmaId retorna lista vazia para turma sem atividades")
    void findByTurmaIdTurmaSemAtividades() {
        assertThat(repository.findByTurmaId(turmaSemAtividades.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByTurmaId retorna lista vazia para turma inexistente")
    void findByTurmaIdTurmaInexistente() {
        assertThat(repository.findByTurmaId(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("existsByProblemasId retorna true quando o problema está em uma atividade")
    void existsByProblemasIdTrue() {
        assertThat(repository.existsByProblemasId(problemaUsado.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByProblemasId retorna true quando o problema está em várias atividades")
    void existsByProblemasIdProblemaCompartilhado() {
        assertThat(repository.existsByProblemasId(problemaCompartilhado.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByProblemasId retorna false para problema sem atividades")
    void existsByProblemasIdFalse() {
        assertThat(repository.existsByProblemasId(problemaLivre.getId())).isFalse();
    }

    @Test
    @DisplayName("existsByProblemasId retorna false para problema inexistente")
    void existsByProblemasIdProblemaInexistente() {
        assertThat(repository.existsByProblemasId(Long.MAX_VALUE)).isFalse();
    }

    @Test
    @DisplayName("existsByProblemasId passa a retornar false depois que o problema é removido da atividade")
    void existsByProblemasIdAposRemocao() {
        AtividadeModel atividade = repository.findByTurmaId(turmaA.getId()).stream()
                .filter(a -> a.getTitulo().equals("Lista 1"))
                .findFirst()
                .orElseThrow();
        ProblemaModel problema = em.find(ProblemaModel.class, problemaUsado.getId());

        atividade.removerProblema(problema);
        repository.saveAndFlush(atividade);
        em.clear();

        assertThat(repository.existsByProblemasId(problemaUsado.getId())).isFalse();
    }

    @Test
    @DisplayName("save persiste a atividade e gera o id")
    void savePersisteAtividade() {
        AtividadeModel nova = new AtividadeModel("Lista nova");
        nova.setTurma(em.find(TurmaModel.class, turmaB.getId()));

        AtividadeModel salva = repository.saveAndFlush(nova);
        em.clear();

        assertThat(salva.getId()).isNotNull();
        Optional<AtividadeModel> encontrada = repository.findById(salva.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTitulo()).isEqualTo("Lista nova");
    }

    @Test
    @DisplayName("save associa os problemas e estratégias da atividade na tabela de junção")
    void savePersisteAssociacoes() {
        AtividadeModel nova = new AtividadeModel("Lista com problema");
        nova.setTurma(em.find(TurmaModel.class, turmaB.getId()));
        nova.adicionarProblema(em.find(ProblemaModel.class, problemaLivre.getId()));

        repository.saveAndFlush(nova);
        em.clear();

        assertThat(repository.existsByProblemasId(problemaLivre.getId())).isTrue();
    }

    @Test
    @DisplayName("save falha quando a atividade não tem turma")
    void saveSemTurmaFalha() {
        AtividadeModel semTurma = new AtividadeModel("Sem turma");

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semTurma));
    }

    @Test
    @DisplayName("save falha quando a atividade não tem título")
    void saveSemTituloFalha() {
        AtividadeModel semTitulo = new AtividadeModel();
        semTitulo.setTurma(em.find(TurmaModel.class, turmaA.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semTitulo));
    }

    @Test
    @DisplayName("deleteById remove a atividade")
    void deleteByIdRemove() {
        Long id = repository.findByTurmaId(turmaB.getId()).get(0).getId();

        repository.deleteById(id);
        repository.flush();

        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.findByTurmaId(turmaB.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteById de uma atividade não remove o problema compartilhado")
    void deleteByIdMantemProblema() {
        Long id = repository.findByTurmaId(turmaB.getId()).get(0).getId();

        repository.deleteById(id);
        repository.flush();
        em.clear();

        assertThat(em.find(ProblemaModel.class, problemaCompartilhado.getId())).isNotNull();
        assertThat(repository.existsByProblemasId(problemaCompartilhado.getId())).isTrue();
    }
}