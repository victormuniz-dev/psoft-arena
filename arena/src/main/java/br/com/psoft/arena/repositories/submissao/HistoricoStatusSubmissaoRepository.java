package br.com.psoft.arena.repositories.submissao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.psoft.arena.models.submissao.HistoricoStatusSubmissaoModel;

@Repository
public interface HistoricoStatusSubmissaoRepository
    extends JpaRepository<HistoricoStatusSubmissaoModel, Long> {

    List<HistoricoStatusSubmissaoModel> findBySubmissaoIdOrderByDataHoraAscIdAsc(
        Long submissaoId
    );
}
