package br.com.psoft.arena.services.submissao;

import java.util.List;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.exception.ResourceNotFoundException;
import br.com.psoft.arena.models.estudante.EstudanteModel;
import br.com.psoft.arena.models.submissao.HistoricoStatusSubmissaoModel;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import br.com.psoft.arena.repositories.estudante.EstudanteRepository;
import br.com.psoft.arena.repositories.submissao.HistoricoStatusSubmissaoRepository;
import br.com.psoft.arena.repositories.submissao.SubmissaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissaoService {

    private static final String MOTIVO_FILA_CHEIA =
        "A fila de processamento de submissões está cheia; "
            + "a submissão não pôde ser encaminhada para avaliação.";

    private final SubmissaoRepository submissaoRepository;
    private final HistoricoStatusSubmissaoRepository historicoRepository;
    private final EstudanteRepository estudanteRepository;
    private final FilaSubmissao filaSubmissao;
    private final SubmissaoProcessador submissaoProcessador;
    private final SubmissaoTransicaoService submissaoTransicaoService;
    private final ValidadorSubmissao validadorSubmissao;

    @Transactional
    public SubmissaoModel registrar(
        Long estudanteId,
        Long atividadeId,
        String codigoFonte
    ) {
        if (estudanteId == null) {
            throw new BusinessRuleException("O estudante deve ser informado.");
        }

        if (atividadeId == null || atividadeId <= 0) {
            throw new BusinessRuleException("A atividade deve ser informada.");
        }

        if (codigoFonte == null || codigoFonte.isBlank()) {
            throw new BusinessRuleException("O código-fonte da submissão não pode ser vazio.");
        }

        validadorSubmissao.validar(estudanteId, atividadeId);

        EstudanteModel estudante = estudanteRepository.findById(estudanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Estudante", estudanteId));

        Integer proximaVersao = submissaoRepository
            .findTopByEstudanteIdAndAtividadeIdOrderByVersaoDesc(estudanteId, atividadeId)
            .map(SubmissaoModel::getVersao)
            .orElse(0) + 1;

        SubmissaoModel submissao = new SubmissaoModel(
            estudante,
            atividadeId,
            codigoFonte,
            proximaVersao
        );

        try {
            return submissaoRepository.save(submissao);
        } catch (DataIntegrityViolationException conflitoDeVersao) {
            throw new BusinessRuleException(
                "Não foi possível registrar a submissão por conflito de versão. Tente novamente."
            );
        }
    }

    @Transactional(readOnly = true)
    public SubmissaoModel buscarPorId(Long submissaoId) {
        return submissaoRepository.findComHistoricoById(submissaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));
    }

    @Transactional(readOnly = true)
    public List<SubmissaoModel> listarPorEstudante(Long estudanteId) {
        if (!estudanteRepository.existsById(estudanteId)) {
            throw new ResourceNotFoundException("Estudante", estudanteId);
        }

        return submissaoRepository.findByEstudanteIdOrderByDataHoraSubmissaoDescVersaoDesc(
            estudanteId
        );
    }

    @Transactional(readOnly = true)
    public SubmissaoModel buscarDoEstudante(Long estudanteId, Long submissaoId) {
        return submissaoRepository.findByIdAndEstudanteId(submissaoId, estudanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Submissão", submissaoId));
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusSubmissaoModel> listarHistorico(
        Long estudanteId,
        Long submissaoId
    ) {
        buscarDoEstudante(estudanteId, submissaoId);

        return historicoRepository.findBySubmissaoIdOrderByDataHoraAscIdAsc(submissaoId);
    }

    public SubmissaoModel encaminharParaProcessamento(Long submissaoId) {
        return encaminhar(submissaoId);
    }

    public SubmissaoModel encaminharParaProcessamento(
        Long submissaoId,
        Long estudanteId
    ) {
        SubmissaoModel submissao = submissaoTransicaoService.buscarDoEstudante(
            submissaoId,
            estudanteId
        );

        return encaminhar(submissao.getId());
    }

    private SubmissaoModel encaminhar(Long submissaoId) {
        SubmissaoModel submissao = submissaoTransicaoService.encaminharParaFila(submissaoId);

        agendarProcessamento(submissao);

        return submissao;
    }

    private void agendarProcessamento(SubmissaoModel submissao) {
        filaSubmissao.enfileirar(submissao.getId());

        if (!filaSubmissao.reservarDrenagem()) {
            return;
        }

        filaSubmissao.registrarTentativaDeAgendamento();

        try {
            submissaoProcessador.processarFila();
        } catch (TaskRejectedException filaCheia) {
            filaSubmissao.remover(submissao.getId());
            filaSubmissao.liberarDrenagem();
            registrarErroDeEnfileiramento(submissao.getId(), filaCheia);
            submissaoTransicaoService.falhar(submissao.getId(), MOTIVO_FILA_CHEIA);

            throw new BusinessRuleException(MOTIVO_FILA_CHEIA);
        } finally {
            filaSubmissao.registrarAgendamentoConcluido();
        }
    }

    private void registrarErroDeEnfileiramento(Long submissaoId, RuntimeException erro) {
        log.error(
            "Fila de processamento recusou a submissão #{}; ela será marcada como Falha.",
            submissaoId,
            erro
        );

        System.err.println(
            "[PSoft Arena] ERRO ao encaminhar a submissão #" + submissaoId
                + " para a fila de processamento: " + erro
                + ". " + MOTIVO_FILA_CHEIA
        );
    }
}
