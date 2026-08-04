package com.newstrust.domain.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskLevelTest {

    @ParameterizedTest
    @CsvSource({
            "100.0, LOW_RISK",
            "70.0, LOW_RISK",
            "69.999, ATTENTION",
            "55.0, ATTENTION",
            "40.0, ATTENTION",
            "39.999, HIGH_RISK",
            "0.0, HIGH_RISK"
    })
    void classifiesScoreIntoExpectedRiskBand(double score, RiskLevel expected) {
        assertEquals(expected, RiskLevel.fromScore(score));
    }
}
