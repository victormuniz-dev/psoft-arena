package br.com.psoft.arena.services.estudante.consultaestudante;

import java.util.List;

import br.com.psoft.arena.dtos.atividade.atividadeEstudante.AtividadeEstudanteResponseDto;
import br.com.psoft.arena.dtos.turma.TurmaEstudante.TurmaEstudanteResponseDto;

/**
 * Contrato de serviço responsável pelas operações de consulta de turmas e atividades
 * sob a perspectiva do estudante .
 */
public interface ConsultaAtividadeEstudanteService {

/**
     * Lista todas as turmas em que o estudante está matriculado.
     *
     * @param estudanteId Identificador único do estudante.
     * @return Lista de DTOs representando as turmas do estudante.
     */
    List<TurmaEstudanteResponseDto> listarTurmas(Long estudanteId);

    /**
     * Lista todas as atividades de uma turma específica em que o estudante participa.
     *
     * @param estudanteId Identificador único do estudante.
     * @param turmaId     Identificador único da turma.
     * @return Lista de DTOs das atividades da turma (abertas e concluídas).
     */
    List<AtividadeEstudanteResponseDto> listarAtividadesPorTurma(Long estudanteId, Long turmaId);

    /**
     * Lista todas as atividades pertencentes a todas as turmas em que o estudante está matriculado.
     *
     * @param estudanteId Identificador único do estudante.
     * @return Lista consolidada de DTOs das atividades do estudante.
     */
    List<AtividadeEstudanteResponseDto> listarAtividades(Long estudanteId);

    /**
     * Busca os detalhes completos de uma atividade específica, validando se o estudante
     * pertence à turma associada à atividade.
     *
     * @param estudanteId Identificador único do estudante.
     * @param atividadeId Identificador único da atividade.
     * @return DTO com os detalhes da atividade.
     */
    AtividadeEstudanteResponseDto buscarAtividade(Long estudanteId, Long atividadeId);
}

