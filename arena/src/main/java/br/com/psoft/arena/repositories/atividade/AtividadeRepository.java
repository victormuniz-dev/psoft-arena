package br.com.psoft.arena.repositories.atividade;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.psoft.arena.models.atividade.AtividadeModel;

public interface AtividadeRepository extends JpaRepository<AtividadeModel, Long> {

    List<AtividadeModel> findByTurmaId(Long turmaId);

    boolean existsByProblemasId(Long problemaId);
}