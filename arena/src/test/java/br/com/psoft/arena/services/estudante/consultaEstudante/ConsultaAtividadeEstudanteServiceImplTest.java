package br.com.psoft.arena.services.estudante.consultaEstudante;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.psoft.arena.dtos.atividade.atividadeEstudante.AtividadeEstudanteResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaEstudante.TurmaEstudanteResponseDto;
import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.atividade.StatusAtividade;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.professor.ProfessorModel;
import br.com.psoft.arena.models.turma.TurmaModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.turma.TurmaRepository;
import br.com.psoft.arena.services.estudante.consultaestudante.ConsultaAtividadeEstudanteServiceImpl;

@ExtendWith(MockitoExtension.class)
class ConsultaAtividadeEstudanteServiceImplTest {

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private EstudanteRepository estudanteRepository;

    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private ConsultaAtividadeEstudanteServiceImpl consultaService;

    private EstudanteModel estudante;
    private TurmaModel turma;
    private AtividadeModel atividade;

    @BeforeEach
    void setUp() {
        ProfessorModel professor = new ProfessorModel("Prof. Giovanni");
        ReflectionTestUtils.setField(professor, "id", 1L);

        
        EstudanteModel estudante = new EstudanteModel("Carlos", "124567855","carlosbom@gmail.com");
        ReflectionTestUtils.setField(estudante, "id", 10L);

        turma = new TurmaModel("Projeto de Software", "PSOFT", "2026.1", professor);
        ReflectionTestUtils.setField(turma, "id", 100L);
        turma.adicionarEstudante(estudante);

        atividade = new AtividadeModel("US7 - Consulta");
        ReflectionTestUtils.setField(atividade, "id", 1000L);
        atividade.setDescricao("Desenvolver consulta de atividades");
        atividade.setPrazo(LocalDateTime.now().plusDays(3));
        atividade.setStatus(StatusAtividade.ABERTA);
        atividade.setTurma(turma);
    }

    @Test
    @DisplayName("Deve listar as turmas em que o estudante está matriculado")
    void deveListarTurmasDoEstudante() {
        when(estudanteRepository.existsById(10L)).thenReturn(true);
        when(turmaRepository.findAllByEstudanteId(10L)).thenReturn(List.of(turma));

        List<TurmaEstudanteResponseDto> resultado = consultaService.listarTurmas(10L);

        assertEquals(1, resultado.size());
        assertEquals("Projeto de Software", resultado.get(0).getNome());
        verify(turmaRepository).findAllByEstudanteId(10L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao listar turmas de estudante inexistente")
    void deveFalharListarTurmasEstudanteInexistente() {
        when(estudanteRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> consultaService.listarTurmas(999L));
    }

    @Test
    @DisplayName("Deve listar atividades de uma turma específica quando estudante é matriculado")
    void deveListarAtividadesPorTurmaComSucesso() {
        when(estudanteRepository.existsById(10L)).thenReturn(true);
        when(turmaRepository.findById(100L)).thenReturn(Optional.of(turma));
        when(atividadeRepository.findAllByTurmaId(100L)).thenReturn(List.of(atividade));

        List<AtividadeEstudanteResponseDto> resultado = consultaService.listarAtividadesPorTurma(10L, 100L);

        assertEquals(1, resultado.size());
        assertEquals("US7 - Consulta", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("Deve bloquear listagem de atividades de turma em que o estudante não participa")
    void deveBloquearListarAtividadesDeTurmaNaoMatriculada() {
        when(estudanteRepository.existsById(20L)).thenReturn(true);
        when(turmaRepository.findById(100L)).thenReturn(Optional.of(turma));

        assertThrows(BusinessRuleException.class, () -> consultaService.listarAtividadesPorTurma(20L, 100L));
    }

    @Test
    @DisplayName("Deve listar todas as atividades do estudante")
    void deveListarTodasAtividadesDoEstudante() {
        when(estudanteRepository.existsById(10L)).thenReturn(true);
        when(atividadeRepository.findAllByEstudanteId(10L)).thenReturn(List.of(atividade));

        List<AtividadeEstudanteResponseDto> resultado = consultaService.listarAtividades(10L);

        assertEquals(1, resultado.size());
        assertEquals("Projeto de Software", resultado.get(0).getNomeTurma());
    }

    @Test
    @DisplayName("Deve buscar detalhes de uma atividade específica quando estudante pertence à turma")
    void deveBuscarAtividadePorIdComSucesso() {
        when(estudanteRepository.existsById(10L)).thenReturn(true);
        when(atividadeRepository.findById(1000L)).thenReturn(Optional.of(atividade));

        AtividadeEstudanteResponseDto resultado = consultaService.buscarAtividade(10L, 1000L);

        assertNotNull(resultado);
        assertEquals(1000L, resultado.getId());
        assertEquals("US7 - Consulta", resultado.getTitulo());
    }

    @Test
    @DisplayName("Deve bloquear consulta de atividade específica se estudante não pertence à turma")
    void deveBloquearBuscarAtividadeDeOutraTurma() {
        when(estudanteRepository.existsById(20L)).thenReturn(true);
        when(atividadeRepository.findById(1000L)).thenReturn(Optional.of(atividade));

        assertThrows(BusinessRuleException.class, () -> consultaService.buscarAtividade(20L, 1000L));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando atividade não existir")
    void deveFalharBuscarAtividadeInexistente() {
        when(estudanteRepository.existsById(10L)).thenReturn(true);
        when(atividadeRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consultaService.buscarAtividade(10L, 9999L));
    }
}
    
    

