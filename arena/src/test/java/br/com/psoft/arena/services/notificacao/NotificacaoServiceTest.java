package br.com.psoft.arena.services.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.dtos.notificacao.NotificacaoResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;
import br.com.psoft.arena.models.avaliacao.NotaFinal;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.notificacao.NotificacaoModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.notificacao.NotificacaoRepository;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private EstudanteRepository estudanteRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    private NotificacaoService notificacaoService;

    private EstudanteModel estudante;
    private SubmissaoModel submissao;

    @BeforeEach
    void setUp() {
        notificacaoService = new NotificacaoService(
            notificacaoRepository,
            estudanteRepository,
            avaliacaoRepository
        );

        estudante = new EstudanteModel("Estudante Teste", "2026001", "teste@email.com");
        ReflectionTestUtils.setField(estudante, "id", 1L);

        submissao = new SubmissaoModel(estudante, 10L, "public class Code {}", 1);
        ReflectionTestUtils.setField(submissao, "id", 100L);
    }

    @Test
    void deveCriarNotificacaoQuandoSubmissaoConcluidaEComAvaliacao() {
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();

        when(avaliacaoRepository.existsBySubmissaoId(100L)).thenReturn(true);
        when(notificacaoRepository.findBySubmissaoId(100L)).thenReturn(Optional.empty());
        when(notificacaoRepository.save(any(NotificacaoModel.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        NotificacaoModel notificacao = notificacaoService.notificarResultadoDisponivel(submissao);

        assertThat(notificacao).isNotNull();
        assertThat(notificacao.getEstudante()).isEqualTo(estudante);
        assertThat(notificacao.getSubmissao()).isEqualTo(submissao);
        assertThat(notificacao.getMensagem()).contains("100", "versão 1", "atividade #10");
        verify(notificacaoRepository, times(1)).save(any(NotificacaoModel.class));
    }

    @Test
    void deveRejeitarNotificacaoQuandoSubmissaoNaoConcluida() {
        assertThatThrownBy(() -> notificacaoService.notificarResultadoDisponivel(submissao))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("Somente submissões concluídas");

        verifyNoInteractions(avaliacaoRepository);
        verifyNoInteractions(notificacaoRepository);
    }

    @Test
    void deveRejeitarNotificacaoQuandoNaoExisteAvaliacaoPersistida() {
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();

        when(avaliacaoRepository.existsBySubmissaoId(100L)).thenReturn(false);

        assertThatThrownBy(() -> notificacaoService.notificarResultadoDisponivel(submissao))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("sem avaliação persistida");

        verify(avaliacaoRepository, times(1)).existsBySubmissaoId(100L);
        verify(notificacaoRepository, never()).save(any(NotificacaoModel.class));
    }

    @Test
    void naoDeveDuplicarNotificacaoAoRepetirOperacao() {
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();

        NotificacaoModel existente = new NotificacaoModel(estudante, submissao, "Mensagem existente");

        when(avaliacaoRepository.existsBySubmissaoId(100L)).thenReturn(true);
        when(notificacaoRepository.findBySubmissaoId(100L)).thenReturn(Optional.of(existente));

        NotificacaoModel resultado = notificacaoService.notificarResultadoDisponivel(submissao);

        assertThat(resultado).isEqualTo(existente);
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void deveListarNotificacoesDoEstudante() {
        when(estudanteRepository.existsById(1L)).thenReturn(true);

        NotificacaoModel notif = new NotificacaoModel(estudante, submissao, "Mensagem 1");
        when(notificacaoRepository.findByEstudanteIdOrderByDataHoraDesc(1L))
            .thenReturn(List.of(notif));

        List<NotificacaoResponseDto> resultado = notificacaoService.listarPorEstudante(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).submissaoId()).isEqualTo(100L);
        assertThat(resultado.get(0).mensagem()).isEqualTo("Mensagem 1");
    }

    @Test
    void deveLancarExcecaoAoListarNotificacoesDeEstudanteInexistente() {
        when(estudanteRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> notificacaoService.listarPorEstudante(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
