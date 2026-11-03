package br.com.psoft.arena.repositories.professor;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.professor.ProfessorModel;

/**
 * Repositório Spring Data JPA para a entidade {@link ProfessorModel}.
 * Fornece operações de persistência e gerenciamento de dados para Professores.
 */
public interface ProfessorRepository  extends JpaRepository<ProfessorModel, Long>{

}