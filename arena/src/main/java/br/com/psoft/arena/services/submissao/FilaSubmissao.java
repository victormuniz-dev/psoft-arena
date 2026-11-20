package br.com.psoft.arena.services.submissao;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class FilaSubmissao {

    private final BlockingQueue<Long> submissaoIds = new LinkedBlockingQueue<>();
    private final AtomicBoolean drenagemAgendada = new AtomicBoolean(false);
    private final AtomicInteger tentativasAgendadas = new AtomicInteger(0);

    public void enfileirar(Long submissaoId) {
        if (submissaoId == null) {
            throw new IllegalArgumentException("O identificador da submissão deve ser informado.");
        }

        submissaoIds.offer(submissaoId);
    }

    public Long retirar() {
        return submissaoIds.poll();
    }

    public boolean remover(Long submissaoId) {
        return submissaoIds.remove(submissaoId);
    }

    public boolean reservarDrenagem() {
        return drenagemAgendada.compareAndSet(false, true);
    }

    public void liberarDrenagem() {
        drenagemAgendada.set(false);
    }

    public boolean estaEmDrenagem() {
        return drenagemAgendada.get();
    }

    public void registrarTentativaDeAgendamento() {
        tentativasAgendadas.incrementAndGet();
    }

    public void registrarAgendamentoConcluido() {
        tentativasAgendadas.decrementAndGet();
    }

    public int tentativasDeAgendamentoEmAndamento() {
        return tentativasAgendadas.get();
    }

    public boolean estaVazia() {
        return submissaoIds.isEmpty();
    }

    public int tamanho() {
        return submissaoIds.size();
    }

    public void limpar() {
        submissaoIds.clear();
        drenagemAgendada.set(false);
        tentativasAgendadas.set(0);
    }
}
