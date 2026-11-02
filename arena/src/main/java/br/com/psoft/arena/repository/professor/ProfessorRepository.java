package br.com.psoft.arena.repository.professor;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.model.professor.ProfessorModel;

public interface ProfessorRepository  extends JpaRepository<ProfessorModel, Long>{

}
