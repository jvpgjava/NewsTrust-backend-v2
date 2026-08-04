package com.newstrust.domain.port.out;

/**
 * Analisa indicadores linguisticos do texto (coerencia semantica, sensacionalismo,
 * pontuacao excessiva) e retorna o fator T (consistencia textual), em escala 0-100.
 */
public interface TextConsistencyAnalyzerPort {

    double analyze(String title, String content);
}
