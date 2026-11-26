package br.com.psoft.arena.services.submissao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.dtos.submissao.HistoricoSubmissaoResponseDto;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.atividade.AtividadeModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.atividade.AtividadeRepository;
import br.com.psoft.arena.repositories.avaliacao.AvaliacaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoSubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional(readOnly = true)
    public List<HistoricoSubmissaoResponseDto> listarHistorico(Long estudanteId) {
        List<SubmissaoModel> submissoes = submissaoRepository
            .findByEstudanteIdOrderByDataHoraSubmissaoDescVersaoDesc(estudanteId);

        if (submissoes.isEmpty()) {
            return List.of();
        }

        Set<Long> atividadeIds = submissoes.stream()
            .map(SubmissaoModel::getAtividadeId)
            .collect(Collectors.toSet());

        Map<Long, AtividadeModel> atividadesPorId = atividadeRepository
            .findAllComTurmaByIdIn(atividadeIds).stream()
            .collect(Collectors.toMap(AtividadeModel::getId, Function.identity()));

        List<Long> submissaoIds = submissoes.stream()
            .map(SubmissaoModel::getId)
            .toList();

        Map<Long, BigDecimal> notasPorSubmissao = avaliacaoRepository
            .findBySubmissaoIdIn(submissaoIds).stream()
            .collect(Collectors.toMap(
                avaliacao -> avaliacao.getSubmissao().getId(),
                avaliacao -> avaliacao.getNotaFinal().getNota()
            ));

        return submissoes.stream()
            .map(submissao -> HistoricoSubmissaoResponseDto.of(
                submissao,
                atividadesPorId.get(submissao.getAtividadeId()),
                notasPorSubmissao.get(submissao.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public HistoricoSubmissaoResponseDto buscarDetalhe(Long estudanteId, Long submissaoId) {
        SubmissaoModel submissao = submissaoRepository
            .findByIdAndEstudanteId(submissaoId, estudanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));

        AtividadeModel atividade = atividadeRepository
            .findAllComTurmaByIdIn(List.of(submissao.getAtividadeId()))
            .stream().findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Atividade", submissao.getAtividadeId()));

        BigDecimal nota = avaliacaoRepository.findBySubmissaoId(submissaoId)
            .map(avaliacao -> avaliacao.getNotaFinal().getNota())
            .orElse(null);

        return HistoricoSubmissaoResponseDto.of(submissao, atividade, nota);
    }
}