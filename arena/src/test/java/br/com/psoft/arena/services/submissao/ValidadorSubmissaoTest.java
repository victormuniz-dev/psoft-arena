package br.com.psoft.arena.services.submissao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.submissao.HistoricoStatusSubmissaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;

@SpringBootTest
class ValidadorSubmissaoTest {

    private static final Long ATIVIDADE_ABERTA = 10L;
    private static final Long ATIVIDADE_FECHADA = 20L;

    @Autowired
    private SubmissaoService submissaoService;

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private SubmissaoRepository submissaoRepository;

    @Autowired
    private HistoricoStatusSubmissaoRepository historicoRepository;

    @Autowired
    private br.com.psoft.arena.repositories.notificacao.NotificacaoRepository notificacaoRepository;

    @Autowired
    private br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository avaliacaoRepository;

    @Test
    void deveDelegarAValidacaoAoValidadorRegistrado() {
        notificacaoRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        historicoRepository.deleteAll();
        submissaoRepository.deleteAll();
        estudanteRepository.deleteAll();

        EstudanteModel estudante = estudanteRepository.save(
            new EstudanteModel("Ana Silva", "2026001", "ana@example.com")
        );

        SubmissaoModel aceita = submissaoService.registrar(
            estudante.getId(),
            ATIVIDADE_ABERTA,
            "public class Main { }"
        );

        assertThat(aceita.getId()).isNotNull();

        assertThatThrownBy(() -> submissaoService.registrar(
                estudante.getId(),
                ATIVIDADE_FECHADA,
                "public class Main { }"
            ))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("não aceita novas submissões");
    }

    @TestConfiguration
    static class ValidadorDaUserStory8 {

        @Bean
        @Primary
        ValidadorSubmissao validadorSubmissaoDaUserStory8() {
            return (estudanteId, atividadeId) -> {
                if (ATIVIDADE_FECHADA.equals(atividadeId)) {
                    throw new BusinessRuleException(
                        "A atividade não aceita novas submissões."
                    );
                }
            };
        }
    }
}
