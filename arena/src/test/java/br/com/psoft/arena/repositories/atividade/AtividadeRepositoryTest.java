package br.com.psoft.arena.repositories.atividade;

import java.time.LocalDateTime;
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
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.estrategia.EstrategiaAvaliacaoModel;
import br.com.psoft.arena.models.estrategia.TipoEstrategia;
import br.com.psoft.arena.models.llm.ConfiguracaoLlmModel;
import br.com.psoft.arena.models.problema.ProblemaModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;

@DataJpaTest
@DisplayName("AtividadeRepository")
class AtividadeRepositoryTest {

    private static final String DESCRICAO = "Descrição da lista";
    private static final String CHAVE_CRIPTOGRAFADA = "Y2hhdmUtY3JpcHRvZ3JhZGE=";
    private static final LocalDateTime PRAZO = LocalDateTime.of(2030, 1, 1, 23, 59);

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
    private EstrategiaAvaliacaoModel estrategia;

    @BeforeEach
    void setUp() {
        professor = em.persist(criarProfessor());

        turmaA = em.persist(criarTurma("Turma A", "TURMA-A", professor));
        turmaB = em.persist(criarTurma("Turma B", "TURMA-B", professor));
        turmaSemAtividades = em.persist(criarTurma("Turma C", "TURMA-C", professor));

        problemaUsado = em.persist(criarProblema("Problema 1"));
        problemaCompartilhado = em.persist(criarProblema("Problema 2"));
        problemaLivre = em.persist(criarProblema("Problema 3"));

        estrategia = em.persist(criarEstrategia());

        persistirAtividade("Lista 1", turmaA, problemaUsado);
        persistirAtividade("Lista 2", turmaA, problemaCompartilhado);
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

    private EstrategiaAvaliacaoModel criarEstrategia() {
        return EstrategiaAvaliacaoModel.builder()
                .nome("Estratégia")
                .tipo(TipoEstrategia.CODE_SMELL)
                .descricao("Descrição da estratégia")
                .promptAvaliacao("Prompt de avaliação")
                .build();
    }

    private AtividadeModel novaAtividade(String titulo, TurmaModel turma, ProblemaModel problema) {
        AtividadeModel atividade = new AtividadeModel(titulo, DESCRICAO, PRAZO);
        atividade.setTurma(turma);
        atividade.definirProblema(problema);
        atividade.definirEstrategia(estrategia);
        atividade.adicionarConfiguracaoLlm(
                new ConfiguracaoLlmModel("GPT", "https://api.exemplo.com", CHAVE_CRIPTOGRAFADA, true));
        return atividade;
    }

    private AtividadeModel persistirAtividade(String titulo, TurmaModel turma, ProblemaModel problema) {
        return em.persist(novaAtividade(titulo, turma, problema));
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
    @DisplayName("existsByProblemaId retorna true quando o problema está associado")
    void existsByProblemaIdTrue() {
        assertThat(repository.existsByProblemaId(problemaUsado.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByProblemaId retorna true quando o problema está em várias atividades")
    void existsByProblemaIdProblemaCompartilhado() {
        assertThat(repository.existsByProblemaId(problemaCompartilhado.getId())).isTrue();
    }

    @Test
    @DisplayName("existsByProblemaId retorna false para problema sem atividades")
    void existsByProblemaIdFalse() {
        assertThat(repository.existsByProblemaId(problemaLivre.getId())).isFalse();
    }

    @Test
    @DisplayName("existsByProblemaId retorna false para problema inexistente")
    void existsByProblemaIdProblemaInexistente() {
        assertThat(repository.existsByProblemaId(Long.MAX_VALUE)).isFalse();
    }

    @Test
    @DisplayName("save persiste a atividade e gera o id")
    void savePersisteAtividade() {
        AtividadeModel nova = novaAtividade("Lista nova", em.find(TurmaModel.class, turmaB.getId()),
                em.find(ProblemaModel.class, problemaLivre.getId()));

        AtividadeModel salva = repository.saveAndFlush(nova);
        em.clear();

        assertThat(salva.getId()).isNotNull();
        Optional<AtividadeModel> encontrada = repository.findById(salva.getId());
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTitulo()).isEqualTo("Lista nova");
    }

    @Test
    @DisplayName("save persiste exatamente um problema e uma estratégia")
    void savePersisteRelacionamentos() {
        AtividadeModel nova = novaAtividade("Lista com relacionamentos",
                em.find(TurmaModel.class, turmaB.getId()),
                em.find(ProblemaModel.class, problemaLivre.getId()));

        AtividadeModel salva = repository.saveAndFlush(nova);
        em.clear();

        AtividadeModel encontrada = repository.findById(salva.getId()).orElseThrow();
        assertThat(encontrada.getProblema().getId()).isEqualTo(problemaLivre.getId());
        assertThat(encontrada.getEstrategia().getId()).isEqualTo(estrategia.getId());
        assertThat(encontrada.getStatus()).isEqualTo(StatusAtividade.ABERTA);
    }

    @Test
    @DisplayName("save persiste a configuração de LLM associada à atividade")
    void savePersisteConfiguracaoLlm() {
        AtividadeModel nova = novaAtividade("Lista com LLM",
                em.find(TurmaModel.class, turmaB.getId()),
                em.find(ProblemaModel.class, problemaLivre.getId()));

        AtividadeModel salva = repository.saveAndFlush(nova);
        Long configuracaoId = salva.getConfiguracoesLlm().get(0).getId();
        em.clear();

        ConfiguracaoLlmModel configuracao = em.find(ConfiguracaoLlmModel.class, configuracaoId);
        assertThat(configuracao).isNotNull();
        assertThat(configuracao.getNomeModelo()).isEqualTo("GPT");
        assertThat(configuracao.getUrlApi()).isEqualTo("https://api.exemplo.com");
        assertThat(configuracao.getChaveApiCriptografada()).isEqualTo(CHAVE_CRIPTOGRAFADA);
        assertThat(configuracao.isPadrao()).isTrue();
        assertThat(configuracao.getAtividade().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("save falha quando a atividade não tem turma")
    void saveSemTurmaFalha() {
        AtividadeModel semTurma = new AtividadeModel("Sem turma", DESCRICAO, PRAZO);
        semTurma.definirProblema(em.find(ProblemaModel.class, problemaLivre.getId()));
        semTurma.definirEstrategia(em.find(EstrategiaAvaliacaoModel.class, estrategia.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semTurma));
    }

    @Test
    @DisplayName("save falha quando a atividade não tem problema")
    void saveSemProblemaFalha() {
        AtividadeModel semProblema = new AtividadeModel("Sem problema", DESCRICAO, PRAZO);
        semProblema.setTurma(em.find(TurmaModel.class, turmaA.getId()));
        semProblema.definirEstrategia(em.find(EstrategiaAvaliacaoModel.class, estrategia.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semProblema));
    }

    @Test
    @DisplayName("save falha quando a atividade não tem estratégia")
    void saveSemEstrategiaFalha() {
        AtividadeModel semEstrategia = new AtividadeModel("Sem estratégia", DESCRICAO, PRAZO);
        semEstrategia.setTurma(em.find(TurmaModel.class, turmaA.getId()));
        semEstrategia.definirProblema(em.find(ProblemaModel.class, problemaLivre.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semEstrategia));
    }

    @Test
    @DisplayName("save falha quando a atividade não tem título")
    void saveSemTituloFalha() {
        AtividadeModel semTitulo = new AtividadeModel();
        semTitulo.setTurma(em.find(TurmaModel.class, turmaA.getId()));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(semTitulo));
    }

    @Test
    @DisplayName("save persiste descrição e prazo de submissão")
    void savePersisteDescricaoEPrazo() {
        AtividadeModel nova = novaAtividade("Lista com dados",
                em.find(TurmaModel.class, turmaA.getId()),
                em.find(ProblemaModel.class, problemaLivre.getId()));

        AtividadeModel salva = repository.saveAndFlush(nova);
        em.clear();

        AtividadeModel encontrada = repository.findById(salva.getId()).orElseThrow();
        assertThat(encontrada.getDescricao()).isEqualTo(DESCRICAO);
        assertThat(encontrada.getPrazoSubmissao()).isEqualTo(PRAZO);
    }

    @Test
    @DisplayName("save falha quando a atividade não tem descrição")
    void saveSemDescricaoFalha() {
        AtividadeModel semDescricao = new AtividadeModel("Sem descrição", null, PRAZO);
        semDescricao.setTurma(em.find(TurmaModel.class, turmaA.getId()));
        semDescricao.definirProblema(em.find(ProblemaModel.class, problemaLivre.getId()));
        semDescricao.definirEstrategia(em.find(EstrategiaAvaliacaoModel.class, estrategia.getId()));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(semDescricao));
    }

    @Test
    @DisplayName("save falha quando a atividade não tem prazo de submissão")
    void saveSemPrazoFalha() {
        AtividadeModel semPrazo = new AtividadeModel("Sem prazo", DESCRICAO, null);
        semPrazo.setTurma(em.find(TurmaModel.class, turmaA.getId()));
        semPrazo.definirProblema(em.find(ProblemaModel.class, problemaLivre.getId()));
        semPrazo.definirEstrategia(em.find(EstrategiaAvaliacaoModel.class, estrategia.getId()));

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(semPrazo));
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
    @DisplayName("deleteById remove a configuração de LLM associada por cascata")
    void deleteByIdRemoveConfiguracaoLlm() {
        AtividadeModel atividade = repository.findByTurmaId(turmaB.getId()).get(0);
        Long configuracaoId = atividade.getConfiguracoesLlm().get(0).getId();
        Long atividadeId = atividade.getId();

        repository.deleteById(atividadeId);
        repository.flush();
        em.clear();

        assertThat(em.find(ConfiguracaoLlmModel.class, configuracaoId)).isNull();
    }

    @Test
    @DisplayName("deleteById de uma atividade não remove o problema e a estratégia compartilhados")
    void deleteByIdMantemRelacionamentosCompartilhados() {
        Long id = repository.findByTurmaId(turmaB.getId()).get(0).getId();

        repository.deleteById(id);
        repository.flush();
        em.clear();

        assertThat(em.find(ProblemaModel.class, problemaCompartilhado.getId())).isNotNull();
        assertThat(em.find(EstrategiaAvaliacaoModel.class, estrategia.getId())).isNotNull();
        assertThat(repository.existsByProblemaId(problemaCompartilhado.getId())).isTrue();
    }
}
