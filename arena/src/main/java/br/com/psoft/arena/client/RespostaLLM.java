package br.com.psoft.arena.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RespostaLLM(Double nota, String evidencias, String justificativa) {}
