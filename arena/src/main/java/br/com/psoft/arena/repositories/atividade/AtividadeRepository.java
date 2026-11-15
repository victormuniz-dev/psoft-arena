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
    boolean existsByProblemasId(Long problemaId);

    /**
     * Busca todas as atividade vinculadas ás turmas em que um estudante esrá matriculado.
     * Inclui tando atividades abertas quando concluídas. 
     * 
     * @param estudanteId Indentificar único do estudante.List<AtividadeModel> findById(Long id);
     * @return Lista de atividade associadas ás turmas do estudante. 
     * 
     */
    @Query("SELECT a FROM AtividadeModel a JOIN a.turma t JOIN t.estudantes e WHERE e.id = :estudanteId")
    List<AtividadeModel> findAllByEstudanteId(@Param("estudanteId") Long estudanteId);
    
    /**
     * Busca todas as atividade associadas a uma Turma específica. 
     * 
     * @param TurmaId Identificador único da turma.
     * @return Lista de atividade da turma  
     */

    List<AtividadeModel> findAllByTurmaId(Long turmaId); 

}