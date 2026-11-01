package br.com.psoft.arena.repositories.professor;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.psoft.arena.models.professor.ProfessorModel;

public interface ProfessorRepository  extends JpaRepository<ProfessorModel, Long>{

}
