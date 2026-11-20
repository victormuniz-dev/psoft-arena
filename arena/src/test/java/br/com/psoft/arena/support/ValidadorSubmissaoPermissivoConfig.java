package br.com.psoft.arena.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import br.com.psoft.arena.services.submissao.ValidadorSubmissao;

@TestConfiguration
public class ValidadorSubmissaoPermissivoConfig {

    @Bean
    @Primary
    ValidadorSubmissao validadorSubmissaoPermissivo() {
        return (estudanteId, atividadeId) -> { };
    }
}