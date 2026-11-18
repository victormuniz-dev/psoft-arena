package br.com.psoft.arena.models.submissao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.psoft.arena.exception.BusinessRuleException;
import br.com.psoft.arena.models.estudante.EstudanteModel;

class SubmissaoModelTest {

    private static final String CODIGO = "public class Main { }";

    private SubmissaoModel criarSubmissao() {
        return new SubmissaoModel(
            new EstudanteModel("Ana Silva", "2026001", "ana@example.com"),
            10L,
            CODIGO,
            1
        );
    }

    private SubmissaoModel criarSubmissaoProcessando() {
        SubmissaoModel submissao = criarSubmissao();
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();

        return submissao;
    }

    @Test
    void deveIniciarNoEstadoSubmetidaComHistoricoInicial() {
        SubmissaoModel submissao = criarSubmissao();

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        assertEquals(1, submissao.getHistorico().size());
        assertEquals(
            StatusSubmissao.SUBMETIDA,
            submissao.getHistorico().get(0).getStatus()
        );
        assertEquals(
            submissao.getDataHoraSubmissao(),
            submissao.getHistorico().get(0).getDataHora()
        );
    }

    @Test
    void devePercorrerTransicoesAteConcluida() {
        SubmissaoModel submissao = criarSubmissao();

        submissao.encaminharParaFila();
        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());

        submissao.iniciarProcessamento();
        assertEquals(StatusSubmissao.PROCESSANDO, submissao.getStatus());

        submissao.concluir();
        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());

        List<StatusSubmissao> estados = submissao.getHistorico()
            .stream()
            .map(HistoricoStatusSubmissaoModel::getStatus)
            .toList();

        assertEquals(
            List.of(
                StatusSubmissao.SUBMETIDA,
                StatusSubmissao.EM_FILA,
                StatusSubmissao.PROCESSANDO,
                StatusSubmissao.CONCLUIDA
            ),
            estados
        );
    }

    @Test
    void deveRegistrarFalhaComMotivo() {
        SubmissaoModel submissao = criarSubmissaoProcessando();

        submissao.falhar("A API de avaliação não respondeu.");

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
        assertEquals(4, submissao.getHistorico().size());
        assertEquals(
            "A API de avaliação não respondeu.",
            submissao.getHistorico().get(3).getObservacao()
        );
    }

    @Test
    void deveAplicarMotivoPadraoQuandoFalhaNaoInformaMotivo() {
        SubmissaoModel submissao = criarSubmissaoProcessando();

        submissao.falhar("   ");

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
        assertFalse(submissao.getHistorico().get(3).getObservacao().isBlank());
    }

    @Test
    void deveFalharAPartirDeEmFilaQuandoOFilaRejeita() {
        SubmissaoModel submissao = criarSubmissao();
        submissao.encaminharParaFila();

        submissao.falhar("A fila de processamento está cheia.");

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
        assertEquals(
            List.of(
                StatusSubmissao.SUBMETIDA,
                StatusSubmissao.EM_FILA,
                StatusSubmissao.FALHA
            ),
            submissao.getHistorico()
                .stream()
                .map(HistoricoStatusSubmissaoModel::getStatus)
                .toList()
        );
    }

    @Test
    void deveImpedirTransicoesInvalidasAPartirDeSubmetida() {
        SubmissaoModel submissao = criarSubmissao();

        assertThrows(BusinessRuleException.class, submissao::iniciarProcessamento);
        assertThrows(BusinessRuleException.class, submissao::concluir);
        assertThrows(BusinessRuleException.class, () -> submissao.falhar("Erro"));

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        assertEquals(1, submissao.getHistorico().size());
    }

    @Test
    void deveImpedirTransicoesInvalidasAPartirDeEmFila() {
        SubmissaoModel submissao = criarSubmissao();
        submissao.encaminharParaFila();

        assertThrows(BusinessRuleException.class, submissao::encaminharParaFila);
        assertThrows(BusinessRuleException.class, submissao::concluir);

        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        assertEquals(2, submissao.getHistorico().size());
    }

    @Test
    void deveImpedirTransicoesInvalidasAPartirDeProcessando() {
        SubmissaoModel submissao = criarSubmissaoProcessando();

        assertThrows(BusinessRuleException.class, submissao::encaminharParaFila);
        assertThrows(BusinessRuleException.class, submissao::iniciarProcessamento);

        assertEquals(StatusSubmissao.PROCESSANDO, submissao.getStatus());
        assertEquals(3, submissao.getHistorico().size());
    }

    @Test
    void deveImpedirTransicoesInvalidasAPartirDeConcluida() {
        SubmissaoModel submissao = criarSubmissaoProcessando();
        submissao.concluir();

        assertThrows(BusinessRuleException.class, submissao::encaminharParaFila);
        assertThrows(BusinessRuleException.class, submissao::iniciarProcessamento);
        assertThrows(BusinessRuleException.class, submissao::concluir);
        assertThrows(BusinessRuleException.class, () -> submissao.falhar("Erro"));

        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());
        assertEquals(4, submissao.getHistorico().size());
    }

    @Test
    void deveImpedirTransicoesInvalidasAPartirDeFalha() {
        SubmissaoModel submissao = criarSubmissaoProcessando();
        submissao.falhar("Falha simulada.");

        assertThrows(BusinessRuleException.class, submissao::encaminharParaFila);
        assertThrows(BusinessRuleException.class, submissao::iniciarProcessamento);
        assertThrows(BusinessRuleException.class, submissao::concluir);
        assertThrows(BusinessRuleException.class, () -> submissao.falhar("Erro"));

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
    }

    @Test
    void deveNotificarCadaMudancaDeEstadoNoTerminal() {
        String notificacoes = capturarNotificacoes(() -> {
            SubmissaoModel submissao = criarSubmissao();
            submissao.encaminharParaFila();
            submissao.iniciarProcessamento();
            submissao.concluir();
        });

        assertTrue(notificacoes.contains("2026001"));
        assertTrue(notificacoes.contains("Submetida"));
        assertTrue(notificacoes.contains("Em fila"));
        assertTrue(notificacoes.contains("Processando"));
        assertTrue(notificacoes.contains("Concluída"));
    }

    @Test
    void deveNotificarFalhaComMotivoETimestamp() {
        String notificacoes = capturarNotificacoes(() -> {
            SubmissaoModel submissao = criarSubmissaoProcessando();
            submissao.falhar("A API de avaliação não respondeu.");
        });

        assertTrue(notificacoes.contains("Falha"));
        assertTrue(notificacoes.contains("A API de avaliação não respondeu."));
        assertTrue(notificacoes.matches("(?s).*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    @Test
    void deveNotificarTransicaoInformandoEstadoAnteriorEAtual() {
        String notificacoes = capturarNotificacoes(() -> {
            SubmissaoModel submissao = criarSubmissao();
            submissao.encaminharParaFila();
        });

        assertTrue(notificacoes.contains("Submetida"));
        assertTrue(notificacoes.contains("Em fila"));
        assertTrue(notificacoes.contains("passou do estado Submetida para o estado Em fila"));
    }

    @Test
    void deveImpedirObservacaoAcimaDoLimiteDoHistorico() {
        SubmissaoModel submissao = criarSubmissao();
        submissao.encaminharParaFila();
        String motivoLongo = "x".repeat(1001);

        BusinessRuleException erro = assertThrows(
            BusinessRuleException.class,
            () -> submissao.transicionarPara(StatusSubmissao.FALHA, motivoLongo)
        );

        assertTrue(erro.getMessage().contains("1000"));
        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        assertEquals(2, submissao.getHistorico().size());
    }

    @Test
    void deveManterHistoricoEmOrdemCronologica() {
        SubmissaoModel submissao = criarSubmissao();
        submissao.encaminharParaFila();
        submissao.iniciarProcessamento();
        submissao.concluir();

        List<LocalDateTime> datas = submissao.getHistorico()
            .stream()
            .map(HistoricoStatusSubmissaoModel::getDataHora)
            .toList();

        for (int indice = 1; indice < datas.size(); indice++) {
            assertFalse(
                datas.get(indice).isBefore(datas.get(indice - 1)),
                "O histórico deve estar em ordem cronológica ascendente."
            );
        }
    }

    @Test
    void deveNotificarOEstadoInicialSubmetidaAoCriarASubmissao() {
        String notificacoes = capturarNotificacoes(this::criarSubmissao);

        assertTrue(notificacoes.contains("2026001"));
        assertTrue(notificacoes.contains("está no estado Submetida"));
        assertTrue(notificacoes.contains("versão 1"));
        assertTrue(notificacoes.matches("(?s).*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
    }

    private String capturarNotificacoes(Runnable acao) {
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
            acao.run();
        } finally {
            System.setOut(saidaOriginal);
        }

        return saida.toString(StandardCharsets.UTF_8);
    }
}
