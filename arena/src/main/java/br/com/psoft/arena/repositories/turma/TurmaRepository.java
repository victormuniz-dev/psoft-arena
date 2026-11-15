package br.com.psoft.arena.repositories.turma;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.psoft.arena.models.turma.TurmaModel;

/**
 * Repositório Spring Data JPA para a entidade {@link TurmaModel}.
 * Fornece operações de persistência e consultas personalizadas para Turmas.
 */
public interface TurmaRepository extends JpaRepository<TurmaModel, Long> {

    /**
     * Verifica se já existe alguma turma cadastrada com o código informado.
     * 
     * @param codigo O código da turma a ser verificado.
     * @return {@code true} se existir uma turma com o código, caso contrário {@code false}.
     */
    boolean existsByCodigo(String codigo);

    /**
     * Lista todas as turmas vinculadas a um professor responsável específico através do ID.
     * 
     * @param professorId O ID do professor responsável.
     * @return Uma lista de {@link TurmaModel} associadas ao professor informado.
     */
    List<TurmaModel> findByProfessorResponsavelId(Long professorId);

    /**
     * Busca todas as turmas em que o estudante está matriculado.
     *
     * @param estudanteId Identificador único do estudante.
     * @return Lista de turmas associadas ao estudante.
     */
    @Query("SELECT DISTINCT t FROM TurmaModel t JOIN t.estudantes e WHERE e.id = :estudanteId")
    List<TurmaModel> findAllByEstudanteId(@Param("estudanteId") Long estudanteId);
}