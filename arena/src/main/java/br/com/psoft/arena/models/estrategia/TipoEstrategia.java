package br.com.psoft.arena.models.estrategia;

/**
 * Enumeração que define os diferentes tipos de estratégias de avaliação
 * suportados pela plataforma PSoft Arena.
 */
public enum TipoEstrategia {
    /**
     * Avaliação focada na detecção de Code Smells (más práticas de código).
     */
    CODE_SMELL,
    /**
     * Avaliação focada em métricas de Acoplamento, Coesão e cumprimento de Princípios de Design (SOLID).
     */
    ACOMPLAMENTO_COESAO_PRINCIPIOS,

    /**
     * Avaliação focada na verificação da correta aplicação de Padrões de Projeto (Design Patterns).
     */
    PADRAO_DE_PROJETO
}