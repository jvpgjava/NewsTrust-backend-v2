package com.newstrust.domain.model;

/**
 * Faixas de risco derivadas do score de credibilidade final (0-100).
 */
public enum RiskLevel {

    LOW_RISK,
    ATTENTION,
    HIGH_RISK;

    public static final double LOW_RISK_THRESHOLD = 70.0;
    public static final double ATTENTION_THRESHOLD = 40.0;

    public static RiskLevel fromScore(double score) {
        if (score >= LOW_RISK_THRESHOLD) {
            return LOW_RISK;
        }
        if (score >= ATTENTION_THRESHOLD) {
            return ATTENTION;
        }
        return HIGH_RISK;
    }
}
