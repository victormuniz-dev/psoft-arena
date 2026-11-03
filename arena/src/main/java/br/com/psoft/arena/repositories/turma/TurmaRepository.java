package br.com.psoft.arena.repositories.turma;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
}