package com.newstrust.domain.service;

import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.SimilarNewsMatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrossVerificationScorerTest {

    private final CrossVerificationScorer scorer = new CrossVerificationScorer();

    @Test
    void returnsNeutralBaselineWhenNoSimilarNewsFound() {
        assertEquals(CrossVerificationScorer.NEUTRAL_BASELINE, scorer.score(List.of()), 1e-9);
        assertEquals(CrossVerificationScorer.NEUTRAL_BASELINE, scorer.score(null), 1e-9);
    }

    @Test
    void fullCorroborationBySimilarLowRiskNewsYieldsMaximumScore() {
        SimilarNewsMatch match = new SimilarNewsMatch(UUID.randomUUID(), "t", 0.95, RiskLevel.LOW_RISK);

        assertEquals(100.0, scorer.score(List.of(match)), 1e-9);
    }

    @Test
    void similarHighRiskNewsYieldsMinimumScore() {
        SimilarNewsMatch match = new SimilarNewsMatch(UUID.randomUUID(), "t", 0.95, RiskLevel.HIGH_RISK);

        assertEquals(0.0, scorer.score(List.of(match)), 1e-9);
    }

    @Test
    void weightsContributionsBySimilarityStrength() {
        SimilarNewsMatch strongLowRisk = new SimilarNewsMatch(UUID.randomUUID(), "a", 0.9, RiskLevel.LOW_RISK);
        SimilarNewsMatch weakHighRisk = new SimilarNewsMatch(UUID.randomUUID(), "b", 0.1, RiskLevel.HIGH_RISK);

        double score = scorer.score(List.of(strongLowRisk, weakHighRisk));

        // (0.9*1.0 + 0.1*0.0) / (0.9+0.1) * 100 = 90
        assertEquals(90.0, score, 1e-9);
    }
}
