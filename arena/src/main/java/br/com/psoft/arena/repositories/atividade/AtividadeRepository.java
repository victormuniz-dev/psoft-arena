package br.com.psoft.arena.repositories.atividade;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.psoft.arena.models.atividade.AtividadeModel;

/**
 * Repositório Spring Data JPA para a entidade {@link AtividadeModel}.
 * Fornece operações de persistência e consultas personalizadas para Atividades.
 */
public interface AtividadeRepository extends JpaRepository<AtividadeModel, Long> {

    /**
     * Lista todas as atividades vinculadas a uma turma específica através do ID da turma.
     * 
     * @param turmaId ID da turma.
     * @return Uma lista de {@link AtividadeModel} associadas à turma informada.
     */
    List<AtividadeModel> findByTurmaId(Long turmaId);

    /**
     * Verifica se existe alguma atividade associada a um determinado problema.
     * 
     * @param problemaId ID do problema.
     * @return {@code true} se existir pelo menos uma atividade vinculada ao problema, caso contrário {@code false}.
     */
    boolean existsByProblemaId(Long problemaId);

    /**
     * Lista todas as atividades vinculadas às turmas em que o estudante está matriculado.
     * Inclui tanto atividades abertas quanto concluídas.
     *
     * @param estudanteId Identificador único do estudante.
     * @return Lista de atividades associadas às turmas do estudante.
     */
    @Query("SELECT DISTINCT a FROM AtividadeModel a JOIN a.turma t JOIN t.estudantes e WHERE e.id = :estudanteId")
    List<AtividadeModel> findAllByEstudanteId(@Param("estudanteId") Long estudanteId);
}