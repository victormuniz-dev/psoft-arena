package br.com.psoft.arena.repositories.turma;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.turma.TurmaModel;

public interface TurmaRepository extends JpaRepository<TurmaModel, Long>{

}
