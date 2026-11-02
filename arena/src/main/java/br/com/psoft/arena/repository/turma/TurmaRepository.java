package br.com.psoft.arena.repository.turma;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.model.turma.TurmaModel;

public interface TurmaRepository extends JpaRepository<TurmaModel, Long> {

    boolean existsByCodigo(String codigo);

    List<TurmaModel> findByProfessorResponsavelId(Long professorId);
}