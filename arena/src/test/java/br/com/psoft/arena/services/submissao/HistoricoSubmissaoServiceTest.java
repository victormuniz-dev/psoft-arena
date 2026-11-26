package br.com.psoft.arena.services.submissao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.dtos.submissao.HistoricoSubmissaoResponseDto;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.submissao.StatusSubmissao;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;

@ExtendWith(MockitoExtension.class)
class HistoricoSubmissaoServiceTest {

    @Mock SubmissaoRepository submissaoRepository;
    @Mock AtividadeRepository atividadeRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @InjectMocks HistoricoSubmissaoService service;

    @Test
    void estudanteSemSubmissoesRecebeListaVazia() {
        when(submissaoRepository
            .findByEstudanteIdOrderByDataHoraSubmissaoDescVersaoDesc(1L))
            .thenReturn(List.of());

        assertTrue(service.listarHistorico(1L).isEmpty());
    }

    @Test
    void submissaoSemAvaliacaoTemNotaNula() {
        SubmissaoModel submissao = mock(SubmissaoModel.class);
        when(submissao.getId()).thenReturn(10L);
        when(submissao.getAtividadeId()).thenReturn(5L);
        when(submissao.getVersao()).thenReturn(2);
        when(submissao.getStatus()).thenReturn(StatusSubmissao.EM_FILA);
        when(submissao.getDataHoraSubmissao()).thenReturn(LocalDateTime.now());

        TurmaModel turma = mock(TurmaModel.class);
        when(turma.getId()).thenReturn(7L);
        when(turma.getNome()).thenReturn("Turma A");

        AtividadeModel atividade = mock(AtividadeModel.class);
        when(atividade.getId()).thenReturn(5L);
        when(atividade.getTitulo()).thenReturn("Atividade 1");
        when(atividade.getTurma()).thenReturn(turma);

        when(submissaoRepository
            .findByEstudanteIdOrderByDataHoraSubmissaoDescVersaoDesc(1L))
            .thenReturn(List.of(submissao));
        when(atividadeRepository.findAllComTurmaByIdIn(Set.of(5L)))
            .thenReturn(List.of(atividade));
        when(avaliacaoRepository.findBySubmissaoIdIn(List.of(10L)))
            .thenReturn(List.of());

        List<HistoricoSubmissaoResponseDto> resultado = service.listarHistorico(1L);

        assertEquals(1, resultado.size());
        assertEquals("Atividade 1", resultado.get(0).atividadeTitulo());
        assertEquals("Turma A", resultado.get(0).turmaNome());
        assertEquals(2, resultado.get(0).versao());
        assertNull(resultado.get(0).nota());
    }
}