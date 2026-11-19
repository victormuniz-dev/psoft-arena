package br.com.psoft.arena.services.submissao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import br.com.psoft.arena.config.AsyncConfig;
import br.com.psoft.arena.models.submissao.SubmissaoModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubmissaoProcessador {

    private static final Logger log = LoggerFactory.getLogger(SubmissaoProcessador.class);

    private static final int TAMANHO_MAXIMO_MOTIVO = 900;
    private static final String MOTIVO_FALHA_PADRAO =
        "Ocorreu um erro que impediu a conclusão da avaliação.";

    private final FilaSubmissao filaSubmissao;
    private final SubmissaoTransicaoService submissaoTransicaoService;
    private final AvaliadorSubmissao avaliadorSubmissao;

    @Async(AsyncConfig.EXECUTOR_SUBMISSAO)
    public void processarFila() {
        boolean continuar = true;

        while (continuar) {
            try {
                Long submissaoId;

                while ((submissaoId = filaSubmissao.retirar()) != null) {
                    processar(submissaoId);
                }
            } finally {
                filaSubmissao.liberarDrenagem();
            }

            continuar = !filaSubmissao.estaVazia() && filaSubmissao.reservarDrenagem();
        }
    }

    public void processar(Long submissaoId) {
        SubmissaoModel submissao;

        try {
            submissao = submissaoTransicaoService.iniciarProcessamento(submissaoId);
        } catch (RuntimeException erroAoIniciar) {
            registrarErroExplicito(submissaoId, erroAoIniciar, "iniciar o processamento");
            marcarComoFalha(submissaoId, motivo(erroAoIniciar));

            return;
        }

        try {
            avaliadorSubmissao.avaliar(submissao);
            submissaoTransicaoService.concluir(submissaoId);
        } catch (RuntimeException erroNaAvaliacao) {
            registrarErroExplicito(submissaoId, erroNaAvaliacao, "concluir a avaliação");
            marcarComoFalha(submissaoId, motivo(erroNaAvaliacao));
        }
    }

    private void marcarComoFalha(Long submissaoId, String motivo) {
        try {
            submissaoTransicaoService.falhar(submissaoId, motivo);
        } catch (RuntimeException erroAoFalhar) {
            registrarErroExplicito(submissaoId, erroAoFalhar, "registrar a falha");
            notificarSubmissaoPresa(submissaoId, motivo);
        }
    }

    private void registrarErroExplicito(
        Long submissaoId,
        RuntimeException erro,
        String etapa
    ) {
        log.error(
            "Falha ao {} da submissão #{}: {}. O erro foi tratado e registrado para diagnóstico.",
            etapa,
            submissaoId,
            erro.toString(),
            erro
        );

        System.err.println(
            "[PSoft Arena] ERRO ao " + etapa + " da submissão #" + submissaoId
                + ": " + erro
                + ". A submissão será marcada como Falha."
        );
    }

    private void notificarSubmissaoPresa(Long submissaoId, String motivo) {
        String mensagem = "[PSoft Arena] ATENÇÃO: a submissão #" + submissaoId
            + " não pôde ser marcada como Falha e pode permanecer em Processando."
            + " Motivo original: " + motivo;

        log.error(mensagem);

        System.err.println(mensagem);
    }

    private String motivo(RuntimeException erro) {
        Throwable causa = erro;

        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }

        String mensagem = causa.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            return MOTIVO_FALHA_PADRAO;
        }

        String motivo = mensagem.strip();

        if (motivo.startsWith(("Falha na avaliação: "))) {
            motivo = motivo.substring("Falha na avaliação: ".length()).strip();
        }

        String descricao = "Falha na avaliação: " + motivo;

        return descricao.length() > TAMANHO_MAXIMO_MOTIVO
            ? descricao.substring(0, TAMANHO_MAXIMO_MOTIVO - 3) + "..."
            : descricao;
    }
}
