package com.newstrust.infrastructure.adapter.out.text;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicTextConsistencyAnalyzerTest {

    private final HeuristicTextConsistencyAnalyzer analyzer = new HeuristicTextConsistencyAnalyzer();

    @Test
    void scoresCleanTextAtTheMaximum() {
        double score = analyzer.analyze("Título normal", "Um conteúdo de notícia perfeitamente comum e sóbrio.");
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void penalizesExcessivePunctuation() {
        double score = analyzer.analyze("Você não vai acreditar nisso!!!", "Conteúdo qualquer.");
        assertThat(score).isLessThan(100.0);
    }

    @Test
    void penalizesSensationalistTerms() {
        double score = analyzer.analyze("URGENTE", "Compartilhe antes que apaguem essa notícia bomba.");
        assertThat(score).isLessThan(70.0);
    }

    @Test
    void neverGoesBelowZero() {
        double score = analyzer.analyze(
                "URGENTE!!! CHOCANTE!!! BOMBA!!!",
                "Você não vai acreditar! Compartilhe antes que apaguem! A mídia esconde a verdade que ninguém conta!!!");
        assertThat(score).isGreaterThanOrEqualTo(0.0);
    }
}
