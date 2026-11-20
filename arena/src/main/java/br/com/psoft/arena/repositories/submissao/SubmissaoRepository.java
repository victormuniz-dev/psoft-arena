package br.com.psoft.arena.repositories.submissao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.submissao.SubmissaoModel;

@Repository
public interface SubmissaoRepository extends JpaRepository<SubmissaoModel, Long> {

    @EntityGraph(attributePaths = {"historico", "estudante"})
    @Query("select submissao from SubmissaoModel submissao where submissao.id = :id")
    Optional<SubmissaoModel> findComHistoricoById(@Param("id") Long id);

    @EntityGraph(attributePaths = "estudante")
    List<SubmissaoModel> findByEstudanteIdOrderByDataHoraSubmissaoDescVersaoDesc(
        Long estudanteId
    );

    @EntityGraph(attributePaths = "estudante")
    List<SubmissaoModel> findByEstudanteIdAndAtividadeIdOrderByVersaoDesc(
        Long estudanteId,
        Long atividadeId
    );

    Optional<SubmissaoModel> findTopByEstudanteIdAndAtividadeIdOrderByVersaoDesc(
        Long estudanteId,
        Long atividadeId
    );

    @EntityGraph(attributePaths = {"historico", "estudante"})
    Optional<SubmissaoModel> findByIdAndEstudanteId(Long id, Long estudanteId);
}
