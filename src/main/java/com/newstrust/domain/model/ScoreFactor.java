package com.newstrust.domain.model;

/**
 * Identifica qual dos quatro fatores (ou o resultado geral) uma {@link ScoreReason} explica.
 */
public enum ScoreFactor {
    SOURCE_REPUTATION,
    TEXTUAL_CONSISTENCY,
    CROSS_VERIFICATION,
    DISSEMINATION_PATTERN,
    OVERALL
}
