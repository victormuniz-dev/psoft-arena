package br.com.psoft.arena.repositories.notificacao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.notificacao.NotificacaoModel;

@Repository
public interface NotificacaoRepository
    extends JpaRepository<NotificacaoModel, Long> {

    boolean existsBySubmissaoId(Long submissaoId);

    Optional<NotificacaoModel> findBySubmissaoId(Long submissaoId);

    @EntityGraph(attributePaths = "submissao")
    List<NotificacaoModel> findByEstudanteIdOrderByDataHoraDesc(Long estudanteId);
}
