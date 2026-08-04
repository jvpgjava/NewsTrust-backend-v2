package com.newstrust.domain.service;

import com.newstrust.domain.model.CredibilityFactors;
import com.newstrust.domain.model.CredibilityScore;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.ScoreFactor;
import com.newstrust.domain.model.ScoreWeights;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredibilityScoreCalculatorTest {

    private final CredibilityScoreCalculator calculator = new CredibilityScoreCalculator();

    @Test
    void appliesDefaultWeightsFromThePaperFormula() {
        // S = 0.35*80 + 0.25*60 + 0.30*90 + 0.10*70 = 28 + 15 + 27 + 7 = 77
        CredibilityFactors factors = CredibilityFactors.of(80, 60, 90, 70);

        CredibilityScore result = calculator.calculate(factors);

        assertEquals(77.0, result.value(), 1e-9);
        assertEquals(RiskLevel.LOW_RISK, result.riskLevel());
        assertEquals(ScoreWeights.DEFAULT, result.weights());
    }

    @Test
    void singleArgOverloadMatchesExplicitDefaultWeights() {
        CredibilityFactors factors = CredibilityFactors.of(45, 30, 20, 60);

        CredibilityScore viaOverload = calculator.calculate(factors);
        CredibilityScore viaExplicitDefault = calculator.calculate(factors, ScoreWeights.DEFAULT);

        assertEquals(viaExplicitDefault.value(), viaOverload.value(), 1e-9);
    }

    @Test
    void missingDisseminationDataFallsBackToNeutralBaselineInsteadOfDistortingScore() {
        // Mesmos R/T/V de appliesDefaultWeightsFromThePaperFormula, mas sem D:
        // S = 0.35*80 + 0.25*60 + 0.30*90 + 0.10*50 = 28 + 15 + 27 + 5 = 75
        CredibilityFactors factors = CredibilityFactors.withoutDisseminationData(80, 60, 90);

        CredibilityScore result = calculator.calculate(factors);

        assertEquals(75.0, result.value(), 1e-9);
        assertTrue(result.reasons().stream()
                .anyMatch(reason -> reason.factor() == ScoreFactor.DISSEMINATION_PATTERN
                        && reason.description().contains("baseline neutro")));
    }

    @Test
    void customDomainWeightsChangeTheOutcomeForTheSameInputData() {
        // Perfil eleitoral: eleva o peso de V (verificacao cruzada) em detrimento de R e T
        ScoreWeights electionProfile = new ScoreWeights(0.20, 0.20, 0.50, 0.10);
        CredibilityFactors factors = CredibilityFactors.of(50, 50, 90, 50);

        CredibilityScore withDefault = calculator.calculate(factors, ScoreWeights.DEFAULT);
        CredibilityScore withElectionProfile = calculator.calculate(factors, electionProfile);

        // default: 0.35*50 + 0.25*50 + 0.30*90 + 0.10*50 = 17.5+12.5+27+5 = 62
        assertEquals(62.0, withDefault.value(), 1e-9);
        // eleitoral: 0.20*50 + 0.20*50 + 0.50*90 + 0.10*50 = 10+10+45+5 = 70
        assertEquals(70.0, withElectionProfile.value(), 1e-9);
        assertTrue(withElectionProfile.value() > withDefault.value());
    }

    @Test
    void scoreAtLowRiskThresholdIsClassifiedAsLowRisk() {
        ScoreWeights passThroughReputation = new ScoreWeights(1.0, 0.0, 0.0, 0.0);
        CredibilityFactors factors = CredibilityFactors.of(70.0, 0, 0, 0);

        CredibilityScore result = calculator.calculate(factors, passThroughReputation);

        assertEquals(RiskLevel.LOW_RISK, result.riskLevel());
    }

    @Test
    void scoreJustBelowLowRiskThresholdIsClassifiedAsAttention() {
        ScoreWeights passThroughReputation = new ScoreWeights(1.0, 0.0, 0.0, 0.0);
        CredibilityFactors factors = CredibilityFactors.of(69.99, 0, 0, 0);

        CredibilityScore result = calculator.calculate(factors, passThroughReputation);

        assertEquals(RiskLevel.ATTENTION, result.riskLevel());
    }

    @Test
    void scoreAtAttentionThresholdIsClassifiedAsAttention() {
        ScoreWeights passThroughReputation = new ScoreWeights(1.0, 0.0, 0.0, 0.0);
        CredibilityFactors factors = CredibilityFactors.of(40.0, 0, 0, 0);

        CredibilityScore result = calculator.calculate(factors, passThroughReputation);

        assertEquals(RiskLevel.ATTENTION, result.riskLevel());
    }

    @Test
    void scoreJustBelowAttentionThresholdIsClassifiedAsHighRisk() {
        ScoreWeights passThroughReputation = new ScoreWeights(1.0, 0.0, 0.0, 0.0);
        CredibilityFactors factors = CredibilityFactors.of(39.99, 0, 0, 0);

        CredibilityScore result = calculator.calculate(factors, passThroughReputation);

        assertEquals(RiskLevel.HIGH_RISK, result.riskLevel());
    }

    @Test
    void extremeInputsProduceScoresAtTheBoundsOfTheZeroToHundredScale() {
        CredibilityScore allZero = calculator.calculate(CredibilityFactors.of(0, 0, 0, 0));
        CredibilityScore allMax = calculator.calculate(CredibilityFactors.of(100, 100, 100, 100));

        assertEquals(0.0, allZero.value(), 1e-9);
        assertEquals(RiskLevel.HIGH_RISK, allZero.riskLevel());
        assertEquals(100.0, allMax.value(), 1e-9);
        assertEquals(RiskLevel.LOW_RISK, allMax.riskLevel());
    }

    @Test
    void producesOneAuditableReasonPerFactorPlusAnOverallExplanation() {
        CredibilityFactors factors = CredibilityFactors.of(80, 60, 90, 70);

        CredibilityScore result = calculator.calculate(factors);

        assertEquals(5, result.reasons().size());
        assertTrue(result.reasons().stream().anyMatch(r -> r.factor() == ScoreFactor.SOURCE_REPUTATION));
        assertTrue(result.reasons().stream().anyMatch(r -> r.factor() == ScoreFactor.TEXTUAL_CONSISTENCY));
        assertTrue(result.reasons().stream().anyMatch(r -> r.factor() == ScoreFactor.CROSS_VERIFICATION));
        assertTrue(result.reasons().stream().anyMatch(r -> r.factor() == ScoreFactor.DISSEMINATION_PATTERN));
        assertTrue(result.reasons().stream().anyMatch(r -> r.factor() == ScoreFactor.OVERALL));
    }
}
