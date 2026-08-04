package com.newstrust.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreWeightsTest {

    @Test
    void defaultWeightsMatchPaperFormula() {
        assertEquals(0.35, ScoreWeights.DEFAULT.reputationWeight());
        assertEquals(0.25, ScoreWeights.DEFAULT.textualConsistencyWeight());
        assertEquals(0.30, ScoreWeights.DEFAULT.crossVerificationWeight());
        assertEquals(0.10, ScoreWeights.DEFAULT.disseminationWeight());
    }

    @Test
    void acceptsCustomWeightsForADomainProfileAsLongAsTheySumToOne() {
        // ex: perfil eleitoral, eleva o peso de V (verificacao cruzada)
        assertDoesNotThrow(() -> new ScoreWeights(0.20, 0.20, 0.50, 0.10));
    }

    @Test
    void rejectsWeightsThatDoNotSumToOne() {
        assertThrows(IllegalArgumentException.class, () -> new ScoreWeights(0.5, 0.5, 0.5, 0.5));
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new ScoreWeights(-0.1, 0.4, 0.4, 0.3));
    }

    @Test
    void toleratesFloatingPointRoundingWhenSummingToOne() {
        assertDoesNotThrow(() -> new ScoreWeights(0.1, 0.2, 0.3, 0.4));
    }
}
