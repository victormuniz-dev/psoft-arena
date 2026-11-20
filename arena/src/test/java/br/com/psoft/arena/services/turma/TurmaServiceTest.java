package br.com.psoft.arena.services.turma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.psoft.arena.dtos.turma.TurmaRequestDto;
import br.com.psoft.arena.dtos.turma.TurmaResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.StatusTurma;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.turma.TurmaRepository;
import br.com.psoft.arena.services.estudante.EstudanteService;
import br.com.psoft.arena.services.professor.ProfessorService;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock private TurmaRepository turmaRepository;
    @Mock private ProfessorService professorService;
    @Mock private EstudanteService estudanteService;
    @InjectMocks private TurmaService turmaService;

    private TurmaRequestDto request(String nome, String codigo, String periodoLetivo, Long professorId) {
        TurmaRequestDto request = new TurmaRequestDto();
        request.setNome(nome);
        request.setCodigo(codigo);
        request.setPeriodoLetivo(periodoLetivo);
        request.setProfessorId(professorId);
        return request;
    }

    @Test
    @DisplayName("Não cria turma com código já existente")
    void codigoDuplicado() {
        when(turmaRepository.existsByCodigo("T01")).thenReturn(true);

        TurmaRequestDto request = request("PSOFT", "T01", "2026.2", 1L);

        assertThrows(BusinessRuleException.class, () -> turmaService.criar(request));
        verify(turmaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cria turma ativa quando os dados são válidos")
    void criaTurma() {
        when(turmaRepository.existsByCodigo("T01")).thenReturn(false);
        when(professorService.buscarPorId(1L)).thenReturn(new ProfessorModel("Maria"));
        when(turmaRepository.save(any(TurmaModel.class))).thenAnswer(i -> i.getArgument(0));

        TurmaRequestDto request = request("PSOFT", "T01", "2026.2", 1L);

        TurmaResponseDto response = turmaService.criar(request);

        assertEquals(StatusTurma.ATIVA, response.getStatus());
    }

    @Test
    @DisplayName("Persiste nome, código, período letivo e professor responsável")
    void persisteTodosOsCamposObrigatorios() {
        ProfessorModel maria = new ProfessorModel("Maria");
        when(turmaRepository.existsByCodigo("T01")).thenReturn(false);
        when(professorService.buscarPorId(1L)).thenReturn(maria);
        when(turmaRepository.save(any(TurmaModel.class))).thenAnswer(i -> i.getArgument(0));

        TurmaRequestDto request = request(" PSOFT ", " T01 ", " 2026.2 ", 1L);

        TurmaResponseDto response = turmaService.criar(request);

        ArgumentCaptor<TurmaModel> captor = ArgumentCaptor.forClass(TurmaModel.class);
        verify(turmaRepository).save(captor.capture());
        TurmaModel salva = captor.getValue();

        assertEquals("PSOFT", salva.getNome());
        assertEquals("T01", salva.getCodigo());
        assertEquals("2026.2", salva.getPeriodoLetivo());
        assertSame(maria, salva.getProfessorResponsavel());

        assertEquals("PSOFT", response.getNome());
        assertEquals("T01", response.getCodigo());
        assertEquals("2026.2", response.getPeriodoLetivo());
        assertEquals("Maria", response.getProfessorNome());
        assertEquals(StatusTurma.ATIVA, response.getStatus());
    }

    @Test
    @DisplayName("Não cria turma quando o professor informado não existe")
    void professorInexistente() {
        when(turmaRepository.existsByCodigo("T01")).thenReturn(false);
        when(professorService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Professor", 99L));

        TurmaRequestDto request = request("PSOFT", "T01", "2026.2", 99L);

        assertThrows(ResourceNotFoundException.class, () -> turmaService.criar(request));
        verify(turmaRepository, never()).save(any());
    }
}
