package br.com.psoft.arena.services.submissao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;

@ExtendWith(MockitoExtension.class)
class ValidadorSubmissaoPadraoTest {

    private static final Long ATIVIDADE_ID = 1L;
    private static final Long ESTUDANTE_DA_TURMA = 10L;
    private static final Long ESTUDANTE_DE_FORA = 99L;

    @Mock private AtividadeRepository atividadeRepository;
    @InjectMocks private ValidadorSubmissaoPadrao validador;

    private TurmaModel turma;

    @BeforeEach
    void prepararTurma() {
        turma = new TurmaModel("PSoft", "T01", "2026.2", new ProfessorModel("Prof"));
        EstudanteModel estudante = new EstudanteModel("Ana", "2026001", "ana@example.com");
        ReflectionTestUtils.setField(estudante, "id", ESTUDANTE_DA_TURMA);
        turma.adicionarEstudante(estudante);
    }

    private AtividadeModel atividade(LocalDateTime prazo) {
        AtividadeModel atividade = new AtividadeModel("Atividade", "Descrição", prazo);
        atividade.setTurma(turma);
        when(atividadeRepository.findById(ATIVIDADE_ID)).thenReturn(Optional.of(atividade));
        return atividade;
    }

    @Test
    void deveAceitarEstudanteDaTurmaEmAtividadeAbertaDentroDoPrazo() {
        atividade(LocalDateTime.now().plusDays(1));
        assertThatCode(() -> validador.validar(ESTUDANTE_DA_TURMA, ATIVIDADE_ID)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarAtividadeInexistente() {
        when(atividadeRepository.findById(ATIVIDADE_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> validador.validar(ESTUDANTE_DA_TURMA, ATIVIDADE_ID))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRejeitarEstudanteQueNaoPertenceATurmaDaAtividade() {
        atividade(LocalDateTime.now().plusDays(1));
        assertThatThrownBy(() -> validador.validar(ESTUDANTE_DE_FORA, ATIVIDADE_ID))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("não pertence à turma");
    }

    @Test
    void deveRejeitarAtividadeConcluida() {
        atividade(LocalDateTime.now().plusDays(1)).encerrar();
        assertThatThrownBy(() -> validador.validar(ESTUDANTE_DA_TURMA, ATIVIDADE_ID))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("não aceita novas submissões");
    }

    @Test
    void deveRejeitarSubmissaoAposOPrazo() {
        atividade(LocalDateTime.now().minusMinutes(1));
        assertThatThrownBy(() -> validador.validar(ESTUDANTE_DA_TURMA, ATIVIDADE_ID))
            .isInstanceOf(BusinessRuleException.class).hasMessageContaining("prazo");
    }
}