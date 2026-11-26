package br.com.psoft.arena.repositories.avaliacao;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.avaliacao.AvaliacaoModel;

@Repository
public interface AvaliacaoRepository extends JpaRepository<AvaliacaoModel, Long> {

    @EntityGraph(attributePaths = "resultados")
    Optional<AvaliacaoModel> findBySubmissaoId(Long submissaoId);

    List<AvaliacaoModel> findBySubmissaoIdIn(Collection<Long> submissaoIds);
}
